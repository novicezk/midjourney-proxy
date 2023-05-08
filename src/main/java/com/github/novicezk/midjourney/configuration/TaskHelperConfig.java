package com.github.novicezk.midjourney.configuration;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.support.task.InMemoryTaskHelper;
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
public class TaskHelperConfig {
    @Bean
    @ConditionalOnProperty(name = "mj.task-store.type", havingValue = "redis")
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
    public TaskHelper taskHelper(ProxyProperties proxyProperties, RedisTemplate<String, Task> redisTemplate) {
        String type = proxyProperties.getTaskStore().getType();
        return switch (type) {
            case "in-memory" -> new InMemoryTaskHelper(proxyProperties);
            case "redis" -> new RedisTaskHelper(proxyProperties, redisTemplate);
            default -> throw new IllegalStateException("Invalid task store type: " + type);
        };
    }
}


