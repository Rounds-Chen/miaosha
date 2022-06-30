package com.example.miaosha.config.interceptors;

import com.auth0.jwt.interfaces.Claim;
import com.example.miaosha.error.BussinessException;
import com.example.miaosha.error.EmBussinessError;
import com.example.miaosha.service.JwtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws BussinessException {
        String headToken = request.getHeader("token"); // 获取请求头中token
        if (StringUtils.isEmpty(headToken)||!headToken.startsWith("bearer;")) {
            return false;
        }
        final String token=headToken.substring(7);// token string

        Map<String, Claim> claims=jwtService.parseToken(token);
        if(claims==null){
            return false;
        }
        // 提取出userId
        request.setAttribute("userId",claims.get("userId").asString());
        return true;
    }
}
