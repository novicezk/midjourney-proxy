package com.github.novicezk.midjourney.service;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.MjDiscordProperties;
import com.github.novicezk.midjourney.result.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MjDiscordServiceImpl implements MjDiscordService {
	private final MjDiscordProperties properties;

	private static final String DISCORD_API_URL = "https://discord.com/api/v9/interactions";

	private String imagineParamsJson;
	private String upParamsJson;

	@PostConstruct
	void init() {
		try {
			this.imagineParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:mj-params/imagine.json").openStream());
			this.upParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:mj-params/up.json").openStream());
		} catch (IOException e) {
			// can't happen
		}
	}

	@Override
	public Message<Void> imagine(String prompt) {
		String paramsStr = this.imagineParamsJson.replace("$guild_id", this.properties.getGuildId())
				.replace("$channel_id", this.properties.getChannelId())
				.replace("$prompt", prompt);
		return postJson(paramsStr);
	}

	@Override
	public Message<Void> up(String messageId, String action, int index, String messageHash) {
		String paramsStr = this.upParamsJson.replace("$guild_id", this.properties.getGuildId())
				.replace("$channel_id", this.properties.getChannelId())
				.replace("$message_id", messageId)
				.replace("$action", action)
				.replace("$index", index + "")
				.replace("$message_hash", messageHash);
		return postJson(paramsStr);
	}

	private Message<Void> postJson(String paramsStr) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", this.properties.getUserToken());
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsStr, headers);
		try {
			ResponseEntity<String> responseEntity = new RestTemplate().postForEntity(DISCORD_API_URL, httpEntity, String.class);
			if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
				return Message.success();
			}
			return Message.of(responseEntity.getStatusCodeValue(), CharSequenceUtil.sub(responseEntity.getBody(), 0, 100));
		} catch (HttpClientErrorException e) {
			try {
				JSONObject error = new JSONObject(e.getResponseBodyAsString());
				return Message.of(error.optInt("code", e.getRawStatusCode()), error.optString("message"));
			} catch (Exception je) {
				return Message.of(e.getRawStatusCode(), CharSequenceUtil.sub(e.getMessage(), 0, 100));
			}
		}
	}
}
