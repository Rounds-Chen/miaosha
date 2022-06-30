package com.example.miaosha.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
    @Autowired
    public RedisTemplate redisTemplate;

    // 设置key的缓存
    public <T> ValueOperations<String, T> setCacheObject(String key, T value) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value);
        return operation;
    }

    // 获取key的缓存
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

}
