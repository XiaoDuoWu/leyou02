package com.leyou.gateway.config;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class LoginFilter extends ZuulFilter {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterProp;
    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
    @Override
    public int filterOrder() {
        return 5;
    }
    @Override
    public boolean shouldFilter() {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest req = ctx.getRequest();
        // 获取路径
        String requestURI = req.getRequestURI();
        // 判断白名单  false 放行 true 拦截
        return !isAllowPath(requestURI);
    }
    private boolean isAllowPath(String requestURI) {
        // 定义一个标记
        boolean flag = false;
        // 遍历允许访问的路径  遍历yml文件中的白名单 判断是否符合  符合返回true
        for (String path : this.filterProp.getAllowPaths()) {
            // 然后判断是否是符合
            if(requestURI.startsWith(path)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public Object run() throws ZuulException {
//        获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
//        获取request
        HttpServletRequest request = ctx.getRequest();
//        获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
//        校验
        try {
            // 校验通过，即放行  token不正确会异常
            JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        } catch (Exception e) {
//            校验出现异常 返回403
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
            logger.error("非法访问，未登录，地址：{}", request.getRemoteHost(),e);
        }
        return null;
    }
}
