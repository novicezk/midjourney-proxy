package com.github.novicezk.midjourney;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mj-proxy")
public class ProxyProperties {
	/**
	 * mj结果通知地址.
	 */
	private String notifyHook;

	private final DiscordConfig discord = new DiscordConfig();

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

}