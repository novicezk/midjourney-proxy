package com.github.novicezk.midjourney;

import com.github.novicezk.midjourney.enums.TranslateWay;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "mj-proxy")
public class ProxyProperties {
	/**
	 * 任务状态变更回调地址.
	 */
	private String notifyHook;
	/**
	 * 用于连接discord，接收、发送消息.
	 */
	private final DiscordConfig discord = new DiscordConfig();

	/**
	 * prompt翻译方式，默认不翻译，可选百度、GPT.
	 */
	private TranslateWay translateWay = TranslateWay.NULL;
	/**
	 * 调用百度翻译把中文的prompt翻译成英文.
	 */
	private final BaiduTranslateConfig baiduTranslate = new BaiduTranslateConfig();
	/**
	 * 调用openai把中文的prompt翻译成英文.
	 */
	private final OpenaiConfig openai = new OpenaiConfig();


	@Data
	public static class DiscordConfig {
		/**
		 * 你的登录token.
		 */
		private String userToken;
		/**
		 * 你的机器人token.
		 */
		private String botToken;
		/**
		 * 你的服务器id.
		 */
		private String guildId;
		/**
		 * 你的频道id.
		 */
		private String channelId;
		/**
		 * Midjourney机器人的名称.
		 */
		private String mjBotName = "Midjourney Bot";
	}

	@Data
	public static class OpenaiConfig {
		/**
		 * gpt的api-key.
		 */
		private String gptApiKey;
		/**
		 * 超时时长.
		 */
		private Duration timeout = Duration.ofSeconds(30);
		/**
		 * 使用的模型.
		 */
		private String model = "gpt-3.5-turbo";
		/**
		 * 返回结果的最大分词数.
		 */
		private int maxTokens = 2048;
		/**
		 * 相似度，取值 0-2.
		 */
		private double temperature = 0;
	}

	@Data
	public static class BaiduTranslateConfig {
		/**
		 * 百度翻译的APP_ID.
		 */
		private String appid;
		/**
		 * 百度翻译的密钥.
		 */
		private String appSecret;

	}

}