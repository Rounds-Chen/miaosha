package com.example.miaosha.config;

import com.example.miaosha.config.interceptors.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

@Configuration
public class InterceptConfig extends WebMvcConfigurationSupport {
    @Resource
    LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器，要声明拦截器对象和要拦截的请求
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") //所有路径都被拦截
                .excludePathPatterns("/user/login") // 排除用户登录请求
                .excludePathPatterns("/user/register"); // 排除用户注册请求
    }
}
