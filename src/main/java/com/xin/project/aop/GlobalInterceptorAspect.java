package com.xin.project.aop;

import cn.hutool.json.JSONUtil;
import com.xin.project.annotation.GlobalInterceptor;
import com.xin.project.common.ErrorCode;
import com.xin.project.constant.UserConstant;
import com.xin.project.exception.BusinessException;
import com.xin.project.utils.JwtUtils;
import com.xin.xincommon.entity.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/23 20:18
 */
@Aspect
@Component("GlobalInterceptor")
public class GlobalInterceptorAspect {
    @Resource
    private StringRedisTemplate redisTemplate;

    @Before("@annotation(com.xin.project.annotation.GlobalInterceptor)")
    public void doInterceptor(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
        if (interceptor == null) {
            return;
        }
        if (interceptor.checkLogin() || interceptor.checkAdmin()) {
            checkLogin(interceptor.checkAdmin());
        }

    }

    public void checkLogin(Boolean checkAdmin) {
        //获取全局的request
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        if (token == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录超时");
        }
        boolean b = JwtUtils.checkToken(token);
        if (!b) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录过期");
        }
        String userStr = redisTemplate.opsForValue().get(UserConstant.USER_LOGIN_TOKEN + token);
        if (userStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录超时");
        }
        User user = JSONUtil.toBean(userStr, User.class);
        if (checkAdmin && !user.getUserRole().equals(UserConstant.ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "接口不存在");
        }
    }
}
