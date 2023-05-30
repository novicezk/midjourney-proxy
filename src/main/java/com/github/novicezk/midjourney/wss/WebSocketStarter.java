package com.github.novicezk.midjourney.wss;

import com.github.novicezk.midjourney.ProxyProperties;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.apache.logging.log4j.util.Strings;

public interface WebSocketStarter {

	void start() throws Exception;

	default void initProxy(ProxyProperties properties) {
		ProxyProperties.ProxyConfig proxy = properties.getProxy();
		if (Strings.isNotBlank(proxy.getHost())) {
			System.setProperty("http.proxyHost", proxy.getHost());
			System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
			System.setProperty("https.proxyHost", proxy.getHost());
			System.setProperty("https.proxyPort", String.valueOf(proxy.getPort()));
		}
	}

	default WebSocketFactory createWebSocketFactory(ProxyProperties properties) {
		ProxyProperties.ProxyConfig proxy = properties.getProxy();
		WebSocketFactory webSocketFactory = new WebSocketFactory().setConnectionTimeout(10000);
		if (Strings.isNotBlank(proxy.getHost())) {
			ProxySettings proxySettings = webSocketFactory.getProxySettings();
			proxySettings.setHost(proxy.getHost());
			proxySettings.setPort(proxy.getPort());
		}
		return webSocketFactory;
	}
}
