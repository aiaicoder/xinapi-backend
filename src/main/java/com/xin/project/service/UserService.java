package com.xin.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.project.common.IdRequest;
import com.xin.project.model.vo.UserVO;
import com.xin.xincommon.entity.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户服务
 *
 * @author xin
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request,HttpServletResponse response);

    /**
     * 更新用户密钥
     * @param idRequest 传入用户id
     * @return
     */
    boolean updateUserKey(IdRequest idRequest,HttpServletRequest request);
    /**
     * 上传头像
     * @param file
     * @param request
     * @return
     */
    boolean uploadFileAvatar(MultipartFile file, HttpServletRequest request);

    boolean updateByUser(User user,HttpServletRequest request);

}
