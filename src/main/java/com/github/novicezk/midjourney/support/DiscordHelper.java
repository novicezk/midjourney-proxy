package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscordHelper {
	private final ProxyProperties properties;
	private static final char[] REGEX_SPECIAL_CHARS = new char[]{'.', '*', '+', '?', '^', '$', '[', ']', '|', '(', ')'};
	/**
	 * DISCORD_SERVER_URL.
	 */
	public static final String DISCORD_SERVER_URL = "https://discord.com";
	/**
	 * DISCORD_CDN_URL.
	 */
	public static final String DISCORD_CDN_URL = "https://cdn.discordapp.com";
	/**
	 * DISCORD_WSS_URL.
	 */
	public static final String DISCORD_WSS_URL = "wss://gateway.discord.gg";

	public String getServer() {
		if (CharSequenceUtil.isBlank(this.properties.getNgDiscord().getServer())) {
			return DISCORD_SERVER_URL;
		}
		String serverUrl = this.properties.getNgDiscord().getServer();
		if (serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}
		return serverUrl;
	}

	public String getCdn() {
		if (CharSequenceUtil.isBlank(this.properties.getNgDiscord().getCdn())) {
			return DISCORD_CDN_URL;
		}
		String cdnUrl = this.properties.getNgDiscord().getCdn();
		if (cdnUrl.endsWith("/")) {
			cdnUrl = cdnUrl.substring(0, cdnUrl.length() - 1);
		}
		return cdnUrl;
	}

	public String getWss() {
		if (CharSequenceUtil.isBlank(this.properties.getNgDiscord().getWss())) {
			return DISCORD_WSS_URL;
		}
		String wssUrl = this.properties.getNgDiscord().getWss();
		if (wssUrl.endsWith("/")) {
			wssUrl = wssUrl.substring(0, wssUrl.length() - 1);
		}
		return wssUrl;
	}

}
