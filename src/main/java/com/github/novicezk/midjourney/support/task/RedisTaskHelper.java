package com.github.novicezk.midjourney.support.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "mj.task.store", havingValue = "redis")
public class RedisTaskHelper implements TaskHelper {
    private static final long EXPIRATION_TIME = 3600 * 24 * 30; // 30 days
    String keyPrefix = "mj::task::";

    @Autowired
    private RedisTemplate<String, Task> redisTemplate;

    public void putTask(String key, Task task) {
        ValueOperations<String, Task> valueOps = redisTemplate.opsForValue();
        valueOps.set(getRedisKey(key), task, EXPIRATION_TIME, TimeUnit.SECONDS);
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