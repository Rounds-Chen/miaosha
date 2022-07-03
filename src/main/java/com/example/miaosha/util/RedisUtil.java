package com.example.miaosha.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    public RedisTemplate redisTemplate;

    // 设置key的缓存
    public <T> ValueOperations<String, T> setCacheObject(String key, T value,long timeout,TimeUnit timeUnit) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value,timeout, timeUnit);
        return operation;
    }

    // 获取key的缓存
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    // 增、减key的缓存
    public long incrementCacheObject(String key,int amout){
        long res=redisTemplate.opsForValue().increment(key,amout);
        return res;
    }

    // 判断obj是否是key对应set的成员
    public <T> boolean inCacheSet(String key,T obj){
        SetOperations operation=redisTemplate.opsForSet();
        return operation.isMember(key,obj);
    }

    // 将obj添加到key对应set中
    public <T> void addInCacheSet(String key,T obj){
        SetOperations operation=redisTemplate.opsForSet();
        operation.add(key,obj);
    }

}
