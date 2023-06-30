package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import lombok.RequiredArgsConstructor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DiscordHelper {
	private final ProxyProperties properties;
	/**
	 * SIMPLE_URL_PREFIX.
	 */
	public static final String SIMPLE_URL_PREFIX = "https://s.mj.run/";
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


	public String getRealPrompt(String prompt) {
		String regex = "<https?://\\S+>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(prompt);
		while (matcher.find()) {
			String url = matcher.group();
			String realUrl = getRealUrl(url.substring(1, url.length() - 1));
			prompt = prompt.replace(url, realUrl);
		}
		return prompt;
	}

	public String getRealUrl(String url) {
		if (!CharSequenceUtil.startWith(url, SIMPLE_URL_PREFIX)) {
			return url;
		}
		ResponseEntity<Void> res = getDisableRedirectRestTemplate().getForEntity(url, Void.class);
		if (res.getStatusCode() == HttpStatus.FOUND) {
			return res.getHeaders().getFirst("Location");
		}
		return url;
	}

	public String findTaskIdWithCdnUrl(String url) {
		if (!CharSequenceUtil.startWith(url, DISCORD_CDN_URL)) {
			return null;
		}
		int hashStartIndex = url.lastIndexOf("/");
		String taskId = CharSequenceUtil.subBefore(url.substring(hashStartIndex + 1), ".", true);
		if (CharSequenceUtil.length(taskId) == 16) {
			return taskId;
		}
		return null;
	}

	private RestTemplate getDisableRedirectRestTemplate() {
		CloseableHttpClient httpClient = HttpClientBuilder.create()
				.disableRedirectHandling()
				.build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return new RestTemplate(factory);
	}

}
