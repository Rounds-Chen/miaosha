package com.example.miaosha.service;

import com.auth0.jwt.interfaces.Claim;

import java.util.Map;

public interface JwtService {
    // 产生jwt token
    String generateToken(String userId);

    // 解析token出payload
    Map<String, Claim> parseToken(String jwt);
}
