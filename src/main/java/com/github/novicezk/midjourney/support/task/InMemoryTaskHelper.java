package com.github.novicezk.midjourney.support.task;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.stream.StreamUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@ConditionalOnProperty(value = "mj.task.store", havingValue = "")
public class InMemoryTaskHelper implements TaskHelper {
	// 创建缓存，1天过期
	private static final TimedCache<String, Task> TASK_MAP = CacheUtil.newTimedCache(3600 * 24 * 1000L);

	public void putTask(String key, Task task) {
		TASK_MAP.put(key, task);
	}

	public void removeTask(String key) {
		TASK_MAP.remove(key);
	}

	public Task getTask(String key) {
		return TASK_MAP.get(key);
	}

	public List<Task> listTask() {
		return ListUtil.toList(TASK_MAP.iterator());
	}

	public Iterator<Task> taskIterator() {
		return TASK_MAP.iterator();
	}

	public Task findById(String taskId) {
		return StreamUtil.of(TASK_MAP.iterator()).filter(t -> taskId.equals(t.getId()))
				.findFirst().orElse(null);
	}

}