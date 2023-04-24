package com.github.novicezk.midjourney.support;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.stream.StreamUtil;
import com.github.novicezk.midjourney.enums.TaskType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MjTaskHelper {
	// 创建缓存，1天过期
	private static final TimedCache<String, MjTask> TASK_MAP = CacheUtil.newTimedCache(3600 * 24 * 1000L);

	public void putTask(String key, MjTask task) {
		TASK_MAP.put(key, task);
	}

	public void removeTask(String key) {
		TASK_MAP.remove(key);
	}

	public MjTask getTask(String key) {
		return TASK_MAP.get(key);
	}

	public List<MjTask> listTask() {
		return ListUtil.toList(TASK_MAP.iterator());
	}

	public MjTask findByTypeAndMessageId(TaskType type, String messageId) {
		return StreamUtil.of(TASK_MAP.iterator()).filter(t -> t.getType() == type && messageId.equals(t.getMessageId()))
				.findFirst().orElse(null);
	}

}