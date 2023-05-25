package com.github.novicezk.midjourney.wss.bot;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.apache.logging.log4j.util.Strings;

import javax.annotation.Resource;

public class BotWebSocketStarter implements WebSocketStarter {
	@Resource
	private BotMessageListener botMessageListener;

	private final ProxyProperties properties;

	public BotWebSocketStarter(ProxyProperties properties) {
		this.properties = properties;
	}

	@Override
	public void start() throws Exception {
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.properties.getDiscord().getBotToken(),
				GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
		builder.addEventListeners(this.botMessageListener);
		ProxyProperties.ProxyConfig proxy = this.properties.getProxy();
		if (Strings.isNotBlank(proxy.getHost())) {
			System.setProperty("http.proxyHost", proxy.getHost());
			System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
			System.setProperty("https.proxyHost", proxy.getHost());
			System.setProperty("https.proxyPort", String.valueOf(proxy.getPort()));
			WebSocketFactory webSocketFactory = new WebSocketFactory();
			ProxySettings proxySettings = webSocketFactory.getProxySettings();
			proxySettings.setHost(proxy.getHost());
			proxySettings.setPort(proxy.getPort());
			builder.setWebsocketFactory(webSocketFactory);
		}
		builder.build();
	}
}
