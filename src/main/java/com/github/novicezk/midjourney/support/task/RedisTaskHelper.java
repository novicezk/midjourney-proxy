package com.github.novicezk.midjourney.support.task;

import com.github.novicezk.midjourney.ProxyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Primary
@ConditionalOnProperty(value = "mj.task-store", havingValue = "redis")
@RequiredArgsConstructor
public class RedisTaskHelper implements TaskHelper {
    String keyPrefix = "mj::task::";

    final ProxyProperties properties;

    private final RedisTemplate<String, Task> redisTemplate;

    public void putTask(String key, Task task) {
        ValueOperations<String, Task> valueOps = redisTemplate.opsForValue();
        valueOps.set(getRedisKey(key), task, properties.getTaskStore().getTimeout(), TimeUnit.SECONDS);
    }

    public void removeTask(String key) {
        redisTemplate.delete(getRedisKey(key));
    }

    public Task getTask(String key) {
        return redisTemplate.opsForValue().get(getRedisKey(key));
    }

    private String getRedisKey(String key) {
        return keyPrefix + key;
    }

    public List<Task> listTask() {
        return Objects.requireNonNull(redisTemplate.keys(getRedisKey("*"))).stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .collect(Collectors.toList());
    }

    public Task findById(String taskId) {
        return listTask().stream()
                .filter(task -> taskId.equals(task.getId()))
                .findFirst().orElse(null);
    }

    public Iterator<Task> taskIterator() {
        return Objects.requireNonNull(redisTemplate.keys(getRedisKey("*"))).stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .toList().iterator();
    }

}