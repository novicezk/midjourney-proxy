package com.github.novicezk.midjourney.service.task;

import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RedisTaskServiceImpl implements TaskService {
	private static final String KEY_PREFIX = "mj-task::";

	private final Duration timeout;
	private final RedisTemplate<String, Task> redisTemplate;

	public RedisTaskServiceImpl(Duration timeout, RedisTemplate<String, Task> redisTemplate) {
		this.timeout = timeout;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void putTask(String id, Task task) {
		this.redisTemplate.opsForValue().set(getRedisKey(id), task, timeout);
	}

	@Override
	public void removeTask(String id) {
		this.redisTemplate.delete(getRedisKey(id));
	}

	@Override
	public Task getTask(String id) {
		return this.redisTemplate.opsForValue().get(getRedisKey(id));
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

	private String getRedisKey(String id) {
		return KEY_PREFIX + id;
	}

}
