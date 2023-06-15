package com.github.novicezk.midjourney.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.support.Task;
import lombok.extern.slf4j.Slf4j;
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

	public NotifyServiceImpl() {
		this.executor = new ThreadPoolTaskExecutor();
		this.executor.setCorePoolSize(10);
		this.executor.setThreadNamePrefix("TaskNotify-");
		this.executor.initialize();
	}

	@Override
	public void notifyTaskChange(Task task) {
		String notifyHook = task.getPropertyGeneric(Constants.TASK_PROPERTY_NOTIFY_HOOK);
		if (CharSequenceUtil.isBlank(notifyHook)) {
			return;
		}
		this.executor.execute(() -> {
			try {
				String paramsStr = OBJECT_MAPPER.writeValueAsString(task);
				ResponseEntity<String> responseEntity = postJson(notifyHook, paramsStr);
				if (responseEntity.getStatusCode() == HttpStatus.OK) {
					log.debug("推送任务变更成功, 任务ID: {}, status: {}", task.getId(), task.getStatus());
				} else {
					log.warn("推送任务变更失败, 任务ID: {}, code: {}, msg: {}", task.getId(), responseEntity.getStatusCodeValue(), responseEntity.getBody());
				}
			} catch (Exception e) {
				log.warn("推送任务变更失败, 任务ID: {}, 描述: {}", task.getId(), e.getMessage());
			}
		});
	}

	private ResponseEntity<String> postJson(String notifyHook, String paramsJson) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsJson, headers);
		return new RestTemplate().postForEntity(notifyHook, httpEntity, String.class);
	}

}
