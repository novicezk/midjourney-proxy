package com.github.novicezk.midjourney.service.task;

import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

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
//		Set<String> keys = this.redisTemplate.keys(getRedisKey("*"));
		//使用scan替换keys
		Set<String> keys = redisTemplate.execute(new RedisCallback<Set<String>>() {

			@Override
			public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {

				Set<String> binaryKeys = new HashSet<>();

				Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(1000).build());
				while (cursor.hasNext()) {
					byte[] next = cursor.next();
					binaryKeys.add(new String(next, StandardCharsets.UTF_8));
				}
				return binaryKeys;
			}
		});
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
