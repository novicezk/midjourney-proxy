package com.github.novicezk.midjourney.service;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
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
public class DiscordServiceImpl implements DiscordService {
	private final ProxyProperties properties;

	private static final String DISCORD_API_URL = "https://discord.com/api/v9/interactions";
	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";

	private String imagineParamsJson;
	private String upscaleParamsJson;
	private String variationParamsJson;
	private String resetParamsJson;

	private String discordUserToken;
	private String discordGuildId;
	private String discordChannelId;

	@PostConstruct
	void init() {
		this.discordUserToken = this.properties.getDiscord().getUserToken();
		this.discordGuildId = this.properties.getDiscord().getGuildId();
		this.discordChannelId = this.properties.getDiscord().getChannelId();
		try {
			this.imagineParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/imagine.json").openStream());
			this.upscaleParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/upscale.json").openStream());
			this.variationParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/variation.json").openStream());
			this.resetParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/reset.json").openStream());
		} catch (IOException e) {
			// can't happen
		}
	}

	@Override
	public Message<Void> imagine(String prompt) {
		String paramsStr = this.imagineParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId);
		JSONObject params = new JSONObject(paramsStr);
		params.getJSONObject("data").getJSONArray("options").getJSONObject(0)
				.put("value", prompt);
		return postJson(params.toString());
	}

	@Override
	public Message<Void> upscale(String messageId, int index, String messageHash) {
		String paramsStr = this.upscaleParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		return postJson(paramsStr);
	}

	@Override
	public Message<Void> variation(String messageId, int index, String messageHash) {
		String paramsStr = this.variationParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		return postJson(paramsStr);
	}

	@Override
	public Message<Void> reset(String messageId, String messageHash) {
		String paramsStr = this.resetParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$message_hash", messageHash);
		return postJson(paramsStr);
	}

	private Message<Void> postJson(String paramsStr) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", this.discordUserToken);
		headers.add("user-agent", USER_AGENT);
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
