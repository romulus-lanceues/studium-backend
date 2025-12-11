package com.lancea.studium.studium_api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {

    private RedisTemplate<String, Object> redisTemplate;

    public RedisTestService(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public void testConnection(){
        redisTemplate.opsForValue().set("test:key", "Hello Redis!");
        String value = (String) redisTemplate.opsForValue().get("test:key");
        System.out.println("Retrieved from Redis: " + value);

    }
}
