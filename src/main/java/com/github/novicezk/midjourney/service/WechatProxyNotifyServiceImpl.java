package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.MjDiscordProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatProxyNotifyServiceImpl implements WechatProxyNotifyService {
	private final MjDiscordProperties properties;

	@Override
	public boolean notifyCreated(String room, String user, String prompt, String messageId) {
		JSONObject params = new JSONObject();
		params.put("type", "created");
		params.put("room", room);
		params.put("user", user);
		params.put("prompt", prompt);
		params.put("messageId", messageId);
		return postJson(params);
	}

	@Override
	public boolean notifyImagine(String room, String user, String prompt, String messageId, String imageUrl) {
		JSONObject params = new JSONObject();
		params.put("type", "image");
		params.put("room", room);
		params.put("user", user);
		params.put("prompt", prompt);
		params.put("messageId", messageId);
		params.put("imageUrl", imageUrl);
		return postJson(params);
	}

	@Override
	public boolean notifyUp(String room, String user, String imageUrl) {
		JSONObject params = new JSONObject();
		params.put("type", "up");
		params.put("room", room);
		params.put("user", user);
		params.put("imageUrl", imageUrl);
		return postJson(params);
	}

	private boolean postJson(JSONObject params) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(params.toString(), headers);
		try {
			ResponseEntity<String> responseEntity = new RestTemplate().postForEntity(this.properties.getWechatProxyHook(), httpEntity, String.class);
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				return true;
			}
			log.warn("回调微信代理失败, code: {}, msg: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
		} catch (HttpClientErrorException e) {
			log.warn("回调微信代理失败, {}", e.getMessage());
		}
		return false;
	}
}
