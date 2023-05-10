package com.github.novicezk.midjourney.service;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.result.Message;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordServiceImpl implements DiscordService {
	private final ProxyProperties properties;
	private static final String DISCORD_API_URL = "https://discord.com/api/v9/interactions";
	private String userAgent;

	private String discordUploadUrl;

	private String imagineParamsJson;
	private String upscaleParamsJson;
	private String variationParamsJson;
	private String resetParamsJson;
	private String describeParamsJson;

	private String discordUserToken;
	private String discordGuildId;
	private String discordChannelId;

	@PostConstruct
	void init() {
		this.discordUserToken = this.properties.getDiscord().getUserToken();
		this.discordGuildId = this.properties.getDiscord().getGuildId();
		this.discordChannelId = this.properties.getDiscord().getChannelId();
		this.discordUploadUrl = "https://discord.com/api/v9/channels/" + this.discordChannelId + "/attachments";
		this.userAgent = this.properties.getDiscord().getUserAgent();
		try {
			this.imagineParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/imagine.json").openStream());
			this.upscaleParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/upscale.json").openStream());
			this.variationParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/variation.json").openStream());
			this.resetParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/reset.json").openStream());
			this.describeParamsJson = IoUtil.readUtf8(ResourceUtils.getURL("classpath:api-params/describe.json").openStream());
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
		return postJsonAndCheckStatus(params.toString());
	}

	@Override
	public Message<Void> upscale(String messageId, int index, String messageHash) {
		String paramsStr = this.upscaleParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		return postJsonAndCheckStatus(paramsStr);
	}

	@Override
	public Message<Void> variation(String messageId, int index, String messageHash) {
		String paramsStr = this.variationParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		return postJsonAndCheckStatus(paramsStr);
	}

	@Override
	public Message<Void> reset(String messageId, String messageHash) {
		String paramsStr = this.resetParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$message_id", messageId)
				.replace("$message_hash", messageHash);
		return postJsonAndCheckStatus(paramsStr);
	}

	@Override
	public Message<String> upload(String fileName, String imageBase64) {
		IDataUrlSerializer serializer = new DataUrlSerializer();
		try {
			DataUrl dataUrl = serializer.unserialize(imageBase64);
			JSONObject fileObj = new JSONObject();
			fileObj.put("filename", fileName);
			fileObj.put("file_size", dataUrl.getData().length);
			fileObj.put("id", "0");
			JSONObject params = new JSONObject()
					.put("files", new JSONArray().put(fileObj));
			ResponseEntity<String> responseEntity = postJson(this.discordUploadUrl, params.toString());
			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				log.error("上传图片到discord失败, status: {}, msg: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
				return Message.of(Message.VALIDATION_ERROR_CODE, "上传图片到discord失败");
			}
			JSONArray array = new JSONObject(responseEntity.getBody()).getJSONArray("attachments");
			if (array.length() == 0) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "上传图片到discord失败");
			}
			String uploadUrl = array.getJSONObject(0).getString("upload_url");
			String uploadFilename = array.getJSONObject(0).getString("upload_filename");
			putFile(uploadUrl, fileName, dataUrl);
			return Message.success(uploadFilename);
		} catch (MalformedURLException e) {
			return Message.of(Message.VALIDATION_ERROR_CODE, "base64格式错误");
		} catch (Exception e) {
			log.error("上传图片到discord失败", e);
			return Message.of(Message.FAILURE_CODE, "上传图片到discord失败");
		}
	}

	@Override
	public Message<Void> describe(String finalFileName) {
		String fileName = CharSequenceUtil.subAfter(finalFileName, "/", true);
		String paramsStr = this.describeParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$file_name", fileName)
				.replace("$final_file_name", finalFileName);
		return postJsonAndCheckStatus(paramsStr);
	}

	private void putFile(String uploadUrl, String fileName, DataUrl dataUrl) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent", this.userAgent);
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		ByteArrayResource fileAsResource = new ByteArrayResource(dataUrl.getData()) {
			@Override
			public String getFilename() {
				return fileName;
			}

			@Override
			public long contentLength() {
				return dataUrl.getData().length;
			}
		};
		params.add("file", fileAsResource);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
		new RestTemplate().put(uploadUrl, requestEntity);
	}

	private ResponseEntity<String> postJson(String paramsStr) {
		return postJson(DISCORD_API_URL, paramsStr);
	}

	private ResponseEntity<String> postJson(String url, String paramsStr) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", this.discordUserToken);
		headers.add("User-Agent", this.userAgent);
		HttpEntity<String> httpEntity = new HttpEntity<>(paramsStr, headers);
		return new RestTemplate().postForEntity(url, httpEntity, String.class);
	}

	private Message<Void> postJsonAndCheckStatus(String paramsStr) {
		try {
			ResponseEntity<String> responseEntity = postJson(paramsStr);
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
