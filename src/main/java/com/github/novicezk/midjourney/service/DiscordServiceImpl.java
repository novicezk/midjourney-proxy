package com.github.novicezk.midjourney.service;


import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.support.DiscordHelper;
import eu.maxschuster.dataurl.DataUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordServiceImpl implements DiscordService {
	private final ProxyProperties properties;
	private final DiscordHelper discordHelper;

	private String discordApiUrl;
	private String userAgent;

	private String discordUploadUrl;
	private String discordSendMessageUrl;

	private String imagineParamsJson;
	private String upscaleParamsJson;
	private String variationParamsJson;
	private String rerollParamsJson;
	private String describeParamsJson;
	private String blendParamsJson;
	private String messageParamsJson;

	private String discordUserToken;
	private String discordGuildId;
	private String discordChannelId;
	private String discordSessionId;

	@PostConstruct
	void init() {
		ProxyProperties.DiscordConfig discord = this.properties.getDiscord();
		this.discordUserToken = discord.getUserToken();
		this.discordGuildId = discord.getGuildId();
		this.discordChannelId = discord.getChannelId();
		this.discordSessionId = discord.getSessionId();
		this.userAgent = discord.getUserAgent();

		String serverUrl = this.discordHelper.getServer();
		this.discordApiUrl = serverUrl + "/api/v9/interactions";
		this.discordUploadUrl = serverUrl + "/api/v9/channels/" + this.discordChannelId + "/attachments";
		this.discordSendMessageUrl = serverUrl + "/api/v9/channels/" + this.discordChannelId + "/messages";

		this.imagineParamsJson = ResourceUtil.readUtf8Str("api-params/imagine.json");
		this.upscaleParamsJson = ResourceUtil.readUtf8Str("api-params/upscale.json");
		this.variationParamsJson = ResourceUtil.readUtf8Str("api-params/variation.json");
		this.rerollParamsJson = ResourceUtil.readUtf8Str("api-params/reroll.json");
		this.describeParamsJson = ResourceUtil.readUtf8Str("api-params/describe.json");
		this.blendParamsJson = ResourceUtil.readUtf8Str("api-params/blend.json");
		this.messageParamsJson = ResourceUtil.readUtf8Str("api-params/message.json");
	}

	@Override
	public Message<Void> imagine(String prompt) {
		String paramsStr = this.imagineParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$session_id", this.discordSessionId);
		JSONObject params = new JSONObject(paramsStr);
		params.getJSONObject("data").getJSONArray("options").getJSONObject(0)
				.put("value", prompt);
		return postJsonAndCheckStatus(params.toString());
	}

	@Override
	public Message<Void> upscale(String messageId, int index, String messageHash, int messageFlags) {
		String paramsStr = this.upscaleParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$session_id", this.discordSessionId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		paramsStr = new JSONObject(paramsStr).put("message_flags", messageFlags).toString();
		return postJsonAndCheckStatus(paramsStr);
	}

	@Override
	public Message<Void> variation(String messageId, int index, String messageHash, int messageFlags) {
		String paramsStr = this.variationParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$session_id", this.discordSessionId)
				.replace("$message_id", messageId)
				.replace("$index", String.valueOf(index))
				.replace("$message_hash", messageHash);
		paramsStr = new JSONObject(paramsStr).put("message_flags", messageFlags).toString();
		return postJsonAndCheckStatus(paramsStr);
	}

	@Override
	public Message<Void> reroll(String messageId, String messageHash, int messageFlags) {
		String paramsStr = this.rerollParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$session_id", this.discordSessionId)
				.replace("$message_id", messageId)
				.replace("$message_hash", messageHash);
		paramsStr = new JSONObject(paramsStr).put("message_flags", messageFlags).toString();
		return postJsonAndCheckStatus(paramsStr);
	}

	@Override
	public Message<Void> describe(String finalFileName) {
		String fileName = CharSequenceUtil.subAfter(finalFileName, "/", true);
		String paramsStr = this.describeParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$session_id", this.discordSessionId)
				.replace("$file_name", fileName)
				.replace("$final_file_name", finalFileName);
		return postJsonAndCheckStatus(paramsStr);
	}

	@Override
	public Message<Void> blend(List<String> finalFileNames, BlendDimensions dimensions) {
		String paramsStr = this.blendParamsJson.replace("$guild_id", this.discordGuildId)
				.replace("$channel_id", this.discordChannelId)
				.replace("$session_id", this.discordSessionId);
		JSONObject params = new JSONObject(paramsStr);
		JSONArray options = params.getJSONObject("data").getJSONArray("options");
		JSONArray attachments = params.getJSONObject("data").getJSONArray("attachments");
		for (int i = 0; i < finalFileNames.size(); i++) {
			String finalFileName = finalFileNames.get(i);
			String fileName = CharSequenceUtil.subAfter(finalFileName, "/", true);
			JSONObject attachment = new JSONObject().put("id", String.valueOf(i))
					.put("filename", fileName)
					.put("uploaded_filename", finalFileName);
			attachments.put(attachment);
			JSONObject option = new JSONObject().put("type", 11)
					.put("name", "image" + (i + 1))
					.put("value", i);
			options.put(option);
		}
		options.put(new JSONObject().put("type", 3)
				.put("name", "dimensions")
				.put("value", "--ar " + dimensions.getValue()));
		return postJsonAndCheckStatus(params.toString());
	}

	@Override
	public Message<String> upload(String fileName, DataUrl dataUrl) {
		try {
			JSONObject fileObj = new JSONObject();
			fileObj.put("filename", fileName);
			fileObj.put("file_size", dataUrl.getData().length);
			fileObj.put("id", "0");
			JSONObject params = new JSONObject()
					.put("files", new JSONArray().put(fileObj));
			ResponseEntity<String> responseEntity = postJson(this.discordUploadUrl, params.toString());
			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				log.error("上传图片到discord失败, status: {}, msg: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
				return Message.of(ReturnCode.VALIDATION_ERROR, "上传图片到discord失败");
			}
			JSONArray array = new JSONObject(responseEntity.getBody()).getJSONArray("attachments");
			if (array.length() == 0) {
				return Message.of(ReturnCode.VALIDATION_ERROR, "上传图片到discord失败");
			}
			String uploadUrl = array.getJSONObject(0).getString("upload_url");
			String uploadFilename = array.getJSONObject(0).getString("upload_filename");
			putFile(uploadUrl, dataUrl);
			return Message.success(uploadFilename);
		} catch (Exception e) {
			log.error("上传图片到discord失败", e);
			return Message.of(ReturnCode.FAILURE, "上传图片到discord失败");
		}
	}

	@Override
	public Message<String> sendImageMessage(String content, String finalFileName) {
		String fileName = CharSequenceUtil.subAfter(finalFileName, "/", true);
		String paramsStr = this.messageParamsJson.replace("$content", content)
				.replace("$channel_id", this.discordChannelId)
				.replace("$file_name", fileName)
				.replace("$final_file_name", finalFileName);
		ResponseEntity<String> responseEntity = postJson(this.discordSendMessageUrl, paramsStr);
		if (responseEntity.getStatusCode() != HttpStatus.OK) {
			log.error("发送图片消息到discord失败, status: {}, msg: {}", responseEntity.getStatusCodeValue(), responseEntity.getBody());
			return Message.of(ReturnCode.VALIDATION_ERROR, "发送图片消息到discord失败");
		}
		JSONObject result = new JSONObject(responseEntity.getBody());
		JSONArray attachments = result.optJSONArray("attachments");
		if (!attachments.isEmpty()) {
			return Message.success(attachments.getJSONObject(0).optString("url"));
		}
		return Message.failure("发送图片消息到discord失败: 图片不存在");
	}

	private void putFile(String uploadUrl, DataUrl dataUrl) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent", this.userAgent);
		headers.setContentType(MediaType.valueOf(dataUrl.getMimeType()));
		headers.setContentLength(dataUrl.getData().length);
		HttpEntity<byte[]> requestEntity = new HttpEntity<>(dataUrl.getData(), headers);
		new RestTemplate().put(uploadUrl, requestEntity);
	}

	private ResponseEntity<String> postJson(String paramsStr) {
		return postJson(discordApiUrl, paramsStr);
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
