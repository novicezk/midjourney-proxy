package com.github.novicezk.midjourney.configuration;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.support.task.RedisTaskHelper;
import com.github.novicezk.midjourney.support.task.Task;
import com.github.novicezk.midjourney.support.task.TaskHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(name = "mj.task-store.type", havingValue = "redis")
public class RedisTaskHelperConfig {

    @Bean
    public RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public TaskHelper taskHelper(ProxyProperties properties, RedisTemplate<String, Task> redisTemplate) {
        return new RedisTaskHelper(properties, redisTemplate);
    }
}

