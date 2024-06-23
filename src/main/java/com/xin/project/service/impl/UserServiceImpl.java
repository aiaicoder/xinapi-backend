package com.xin.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.project.common.IdRequest;
import com.xin.project.constant.RedisKeyConstant;
import com.xin.project.constant.UserConstant;
import com.xin.project.exception.BusinessException;
import com.xin.project.common.ErrorCode;
import com.xin.project.mapper.UserMapper;
import com.xin.project.model.vo.UserVO;
import com.xin.project.service.UserService;
import com.xin.project.utils.FileUploadUtil;
import com.xin.project.utils.JwtUtils;
import com.xin.xincommon.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Year;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * 用户服务实现类
 *
 * @author yupi
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    public static final long LIMITIME = 2592000000L;
    public static final String USER_AVATAR = "https://my-notes-li.oss-cn-beijing.aliyuncs.com/li/%E9%B8%A1%E5%93%A5.png";

    String DEFAULT_USERNAME = "用户-"+String.valueOf(System.currentTimeMillis()).substring(0,5);

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedissonClient redissonClient;


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "xinzz";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            //3.分配accessKey,secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(3));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setGender(0);
            user.setUserName(DEFAULT_USERNAME);
            //插入默认头像
            user.setUserAvatar(USER_AVATAR);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
            }
            return user.getId();
        }
    }

    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        UUID uuid = UUID.fastUUID();
        String newToken = uuid.toString(true);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        user.setToken(newToken);
        return setLoginUser(user,newToken);
    }

    /**
     * 设置用户登录态
     * @param user
     * @return
     */
    private UserVO setLoginUser(User user,String token) {
        String userToken = JwtUtils.getJwtToken(user.getId(), user.getUserName());
        String userJson = getUserJson(user);
        stringRedisTemplate.opsForValue().set(UserConstant.USER_LOGIN_STATE + user.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        stringRedisTemplate.opsForValue().set(UserConstant.USER_LOGIN_TOKEN + token, userToken, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return this.getUserVO(user);
    }

    /**
     * 将user封装成userVo
     * @param user
     * @return
     */
    private UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String jwtToken = stringRedisTemplate.opsForValue().get(UserConstant.USER_LOGIN_TOKEN + token);
        if (jwtToken == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (!JwtUtils.checkToken(jwtToken)){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"登录过期");
        }
        Long userId = JwtUtils.getUserIdByToken(jwtToken);
        String userStr = stringRedisTemplate.opsForValue().get(UserConstant.USER_LOGIN_STATE + userId);
        if (userStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return JSONUtil.toBean(userStr, User.class);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("token");
        String jwtToken = stringRedisTemplate.opsForValue().get(UserConstant.USER_LOGIN_TOKEN + token);
        Long userId= JwtUtils.getUserIdByToken(jwtToken);
        Boolean deleteUser = stringRedisTemplate.delete(UserConstant.USER_LOGIN_STATE + userId);
        Boolean deleteJwtToken = stringRedisTemplate.delete(UserConstant.USER_LOGIN_TOKEN + token);
        if (Boolean.TRUE.equals(deleteJwtToken) && Boolean.TRUE.equals(deleteUser)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to delete login state from Redis");
        }
        return true;
}


    /**
     * 修改用户密钥
     * @param idRequest 传入用户id
     * @param request
     * @return
     */
    @Override
    public boolean updateUserKey(IdRequest idRequest, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        User oldUser = this.getById(idRequest.getId());
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        //30天之内只能修改一次密钥
        Date lastUpdateKey = oldUser.getLastUpdateKey();
        if (lastUpdateKey != null){
            long lastUpdate = lastUpdateKey.getTime();
            if (System.currentTimeMillis() - lastUpdate < LIMITIME) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密钥修改过于频繁,30天只能修改一次密钥");
            }
        }
        //为空说明是第一次修改，不拦截
        RLock lock = redissonClient.getLock(RedisKeyConstant.USER_CHANGE_KEY_PREFIX + oldUser.getId());
        //加锁防止频繁更改密钥
        try {
            boolean b = lock.tryLock(0, TimeUnit.MILLISECONDS);
            if (b) {
                String userAccount = oldUser.getUserAccount();
                String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(4));
                String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
                oldUser.setAccessKey(accessKey);
                oldUser.setSecretKey(secretKey);
                oldUser.setLastUpdateKey(new Date());
                //先更新缓存
                boolean b1 = this.updateById(oldUser);
                if (b1){
                    String userJson = getUserJson(oldUser);
                    stringRedisTemplate.opsForValue().set(UserConstant.USER_LOGIN_STATE + loginUser.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
                }
                return b1;
            }
        } catch (InterruptedException e) {
            log.error(" " + e);
        } finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public boolean uploadFileAvatar(MultipartFile file, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        //更新持久层用户头像信息
        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        String url = FileUploadUtil.uploadFileAvatar(file);
        updateUser.setUserAvatar(url);
        boolean result = this.updateById(updateUser);
        if (result){
            //更新用户缓存
            loginUser.setUserAvatar(url);
            String userJson = getUserJson(loginUser);
            stringRedisTemplate.opsForValue().set(UserConstant.USER_LOGIN_STATE + loginUser.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    @Override
    public boolean updateByUser(User user,HttpServletRequest request) {
        boolean b = this.updateById(user);
        if (b){
            User userTotal = this.getById(user.getId());
            String userJson = getUserJson(userTotal);
            stringRedisTemplate.opsForValue().set(UserConstant.USER_LOGIN_STATE + user.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        }
        return b;
    }

    private static String getUserJson(User loginUser) {
        return JSONUtil.toJsonStr(loginUser);
    }

}




