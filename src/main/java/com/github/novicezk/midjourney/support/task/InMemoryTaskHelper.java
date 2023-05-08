package com.github.novicezk.midjourney.support.task;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import com.github.novicezk.midjourney.ProxyProperties;

import java.util.List;


public class InMemoryTaskHelper implements TaskHelper {

	// 创建缓存
	private final TimedCache<String, Task> taskMap;

	public InMemoryTaskHelper(ProxyProperties properties) {
		taskMap = CacheUtil.newTimedCache(properties.getTaskStore().getTimeout().toMillis());
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
