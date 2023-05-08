package com.github.novicezk.midjourney.support.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RedisTaskHelper implements TaskHelper {
	private static final String KEY_PREFIX = "mj::task::";
	private final Duration timeout;

	@Autowired
	private RedisTemplate<String, Task> redisTemplate;

	public RedisTaskHelper(Duration taskExpiration) {
		this.timeout = taskExpiration;
	}

	@Override
	public void putTask(String key, Task task) {
		this.redisTemplate.opsForValue().set(getRedisKey(key), task, timeout);
	}

	@Override
	public void removeTask(String key) {
		this.redisTemplate.delete(getRedisKey(key));
	}

	@Override
	public Task getTask(String key) {
		return this.redisTemplate.opsForValue().get(getRedisKey(key));
	}

	@Override
	public List<Task> listTask() {
		Set<String> keys = this.redisTemplate.keys(getRedisKey("*"));
		if (keys == null || keys.isEmpty()) {
			return Collections.emptyList();
		}
		ValueOperations<String, Task> operations = this.redisTemplate.opsForValue();
		return keys.stream().map(operations::get)
				.filter(Objects::nonNull)
				.toList();
	}

	private String getRedisKey(String key) {
		return KEY_PREFIX + key;
	}

}
