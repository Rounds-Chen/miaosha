package com.example.miaosha.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.miaosha.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.token.key}")
    private String KEY;

    @Value("${jwt.token.expiration}")
    private long EXPIRATION;

    @Override
    public String generateToken(String userId) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        return JWT.create()
                .withHeader(map)// 添加头部
                //可以将基本信息放到claims中
                .withClaim("userId", userId)//
                .withExpiresAt(expireDate) //超时设置,设置过期的日期
                .withIssuedAt(new Date()) //签发时间
                .sign(Algorithm.HMAC256(KEY));
    }

    @Override
    public Map<String,Claim> parseToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaims();
    }
}
