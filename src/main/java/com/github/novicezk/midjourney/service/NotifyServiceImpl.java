package com.github.novicezk.midjourney.service;

import cn.hutool.core.text.CharSequenceUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private final ThreadPoolTaskExecutor executor;
	private final Map<String, Object> taskLocks = new ConcurrentHashMap<>();

	public NotifyServiceImpl(ProxyProperties properties) {
		this.executor = new ThreadPoolTaskExecutor();
		this.executor.setCorePoolSize(properties.getQueue().getNotifyPoolSize());
		this.executor.setThreadNamePrefix("TaskNotify-");
		this.executor.initialize();
	}

	@Override
	public void notifyTaskChange(Task task) {
		String notifyHook = task.getPropertyGeneric(Constants.TASK_PROPERTY_NOTIFY_HOOK);
		if (CharSequenceUtil.isBlank(notifyHook)) {
			return;
		}
		// 获取线程所需的参数，避免在线程中获取
		String tastId = task.getId();
		TaskStatus taskStatus = task.getStatus();
		Object taskLock = taskLocks.computeIfAbsent(tastId, id -> new Object()); // 获取任务对应的锁对象，避免同一任务id进度推送顺序错乱问题
		log.debug("创建任务变更线程, 任务ID: {}, status: {}", tastId, taskStatus);
		try {
			String paramsStr = OBJECT_MAPPER.writeValueAsString(task);
			this.executor.execute(() -> {
				synchronized (taskLock) {
					try {
						log.debug("开始推送任务变更, 任务ID: {}, status: {}", tastId, taskStatus);
						ResponseEntity<String> responseEntity = postJson(notifyHook, paramsStr);
						if (responseEntity.getStatusCode() == HttpStatus.OK) {
							log.debug("推送任务变更成功, 任务ID: {}, status: {}", tastId, taskStatus);
						} else {
							log.warn("推送任务变更失败, 任务ID: {}, code: {}, msg: {}", tastId, responseEntity.getStatusCodeValue(), responseEntity.getBody());
						}
					} catch (Exception e) {
						log.warn("推送任务变更失败, 任务ID: {}, 描述: {}", tastId, e.getMessage());
					}
				}
			});
		} catch (JsonProcessingException e) {
			log.warn("创建任务ID: {}, status: {}, 描述: {}", tastId, taskStatus, e.getMessage());
		}
		
	}

	private ResponseEntity<String> postJson(String notifyHook, String paramsJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsJson, headers);
		return new RestTemplate().postForEntity(notifyHook, httpEntity, String.class);
	}

}
