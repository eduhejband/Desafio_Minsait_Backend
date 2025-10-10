package com.example.desafio_back.config;

import com.example.desafio_back.dtos.CacheBalance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, CacheBalance> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, CacheBalance> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);

        var keySer   = new StringRedisSerializer();
        var valueSer = new GenericJackson2JsonRedisSerializer();

        t.setKeySerializer(keySer);
        t.setValueSerializer(valueSer);
        t.setHashKeySerializer(keySer);
        t.setHashValueSerializer(valueSer);

        return t;
    }
}
