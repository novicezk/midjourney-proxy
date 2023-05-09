package com.github.novicezk.midjourney.service.task;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;

import java.time.Duration;
import java.util.List;


public class InMemoryTaskServiceImpl implements TaskService {
	private final TimedCache<String, Task> taskMap;

	public InMemoryTaskServiceImpl(Duration timeout) {
		this.taskMap = CacheUtil.newTimedCache(timeout.toMillis());
	}

	@Override
	public void putTask(String key, Task task) {
		this.taskMap.put(key, task);
	}

	@Override
	public void removeTask(String key) {
		this.taskMap.remove(key);
	}

	@Override
	public Task getTask(String key) {
		return this.taskMap.get(key);
	}

	@Override
	public List<Task> listTask() {
		return ListUtil.toList(this.taskMap.iterator());
	}

}
