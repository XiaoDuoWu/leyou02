package com.leyou.order.filter;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
    //    里面有cookieName 公钥地址 公钥对象
    private JwtProperties jwtProperties;
    //    定义一个线程域， 存放当前线程的用户信息
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public LoginInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 前置方法 获取token 放入线程域中
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
//        获取token
            String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
            if (StringUtils.isBlank(token)) {
                throw new LyException(ExceptionEnum.TOKEN_ERROR);
            }
//        根据token 和 公钥 获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
//        把用户信息 放入 tl 线程域中
            tl.set(userInfo);
            return true;
        } catch (Exception e) {
            log.error("解析获取信息过程出错", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    /**
     *   后置处理方法  释放线程域资源
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    //获取当先线程域中的用户信息
    public static UserInfo getLoginUser() {
        return tl.get();
    }
}
