package com.xin.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.project.model.dto.userInterfaceInfo.UpdateUserInterfaceInfoDTO;
import com.xin.project.common.ErrorCode;
import com.xin.project.constant.FreeInterfaceConstant;
import com.xin.project.constant.RedisKeyConstant;
import com.xin.project.exception.BusinessException;
import com.xin.project.mapper.UserInterfaceInfoMapper;
import com.xin.project.service.UserInterfaceInfoService;
import com.xin.xincommon.entity.UserInterfaceInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
* @author Administrator
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2023-11-07 19:03:42
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {

    public static final int TIME = 24 * 60 * 60 * 1000;
    @Resource
    private RedissonClient redissonClient;

    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = userInterfaceInfo.getUserId();
        Long id = userInterfaceInfo.getId();

        // 创建时，所有参数必须非空
        if (add) {
            if (userId <=0 || id <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在，或接口不存在");
            }
        }

        if (userInterfaceInfo.getLeftNum() < 0){

            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口调用次数不能小于0");
        }
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.eq("userId", userId);
        updateWrapper.gt("leftNum",0);
        updateWrapper.setSql("leftNum = leftNum -1,  totalNum = totalNum+1");
        int left = getLeftNum(interfaceInfoId,userId);
        if (left <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口剩余调用次数不足");
        }
        RLock lock = redissonClient.getLock(RedisKeyConstant.REDIS_KEY_PREFIX + interfaceInfoId);
        try {
            boolean b = lock.tryLock(0, TimeUnit.MILLISECONDS);
            if (b){
                return this.update(updateWrapper);
            }
        }catch (InterruptedException e){
            log.error(""+e);
        }finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public int getLeftNum(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        queryWrapper.eq("userId", userId);
        UserInterfaceInfo userInterfaceInfo = getOne(queryWrapper);
        if (userInterfaceInfo == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return userInterfaceInfo.getLeftNum();
    }

    @Override
    public boolean UpdateUserInterfaceInfo(UpdateUserInterfaceInfoDTO updateUserInterfaceInfoDTO) {
        Long interfaceId = updateUserInterfaceInfoDTO.getInterfaceId();
        Long userId = updateUserInterfaceInfoDTO.getUserId();
        RLock lock = redissonClient.getLock(RedisKeyConstant.USER_COUNT_KEY_PREFIX + interfaceId);
        try {
            boolean b = lock.tryLock(0, TimeUnit.MILLISECONDS);
            //加锁防止刷次数
            if (b){
                QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("interfaceInfoId", interfaceId);
                queryWrapper.eq("userId", userId);
                UserInterfaceInfo one = getOne(queryWrapper);

                UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("interfaceInfoId", interfaceId);
                updateWrapper.eq("userId", userId);
                updateWrapper.setSql("lastFreeTime = now()");
                //表明是获取免费次数
                if (one!=null){
                    Date createTime = one.getLastFreeTime();
                    //每隔24小时只能获取一次免费次数，在24小时内不能重复获取
                    //表示第一次获取
                    if (createTime != null){
                        long time = createTime.getTime();
                        long l = System.currentTimeMillis();
                        if ( time + TIME > l){
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"24小时内只能获取一次免费次数");
                        }
                    }
                    //剩余次数不能超过1000
                    if (one.getLeftNum() + FreeInterfaceConstant.FREE_NUMBER < 1000){
                        updateWrapper.setSql("leftNum = leftNum + "  + FreeInterfaceConstant.FREE_NUMBER);
                        return this.update(updateWrapper);
                    }else{
                        //如果当前调用次数加上100已经大于1000，就直接让他等于1000
                        if (one.getLeftNum() + FreeInterfaceConstant.FREE_NUMBER > 1000){
                            updateWrapper.setSql("leftNum = 1000");
                            return this.update(updateWrapper);
                        }else if (one.getLeftNum() == 1000){
                            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"免费次数已达上限");
                        }
                    }
                }
                //表示第一次获取，直接添加新的值
                else {
                    UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
                    userInterfaceInfo.setUserId(userId);
                    userInterfaceInfo.setInterfaceInfoId(interfaceId);
                    userInterfaceInfo.setLeftNum(FreeInterfaceConstant.FREE_NUMBER);
                    return this.save(userInterfaceInfo);
                }
            }
        }catch (InterruptedException e){
            log.error(""+e);
        }finally {
            lock.unlock();
        }
        return false;
    }


}




