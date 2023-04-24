package com.github.novicezk.midjourney;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mj-discord")
public class MjDiscordProperties {
	/**
	 * 微信代理-mj结果通知接口.
	 */
	private String wechatProxyHook = "http://localhost:4120/wechat-mj/notify";

	/**
	 * 你的登录token.
	 */
	private String userToken;
	/**
	 * 你的机器人token.
	 */
	private String botToken;

	/**
	 * Midjourney机器人的名称.
	 */
	private String mjBotName = "Midjourney Bot";
	/**
	 * 你的服务器id.
	 */
	private String guildId;
	/**
	 * 你的频道id.
	 */
	private String channelId;

}