package com.github.novicezk.midjourney.support;

import javax.annotation.Resource;

import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.github.novicezk.midjourney.ProxyProperties;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

@Component
public class DiscordStarter implements ApplicationListener<ApplicationStartedEvent> {
	@Resource
	private ProxyProperties properties;
	@Resource
	private DiscordMessageListener discordMessageListener;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.properties.getDiscord().getBotToken(),
				GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
		builder.addEventListeners(this.discordMessageListener);
		ProxyProperties.ProxyConfig proxy = this.properties.getProxy();
		if (Strings.isNotBlank(proxy.getHost()) && Strings.isNotBlank(proxy.getPort())){
			System.setProperty("http.proxyHost", proxy.getHost());
			System.setProperty("http.proxyPort", proxy.getPort());
			System.setProperty("https.proxyHost", proxy.getHost());
			System.setProperty("https.proxyPort", proxy.getPort());
			WebSocketFactory webSocketFactory = new WebSocketFactory();
			ProxySettings proxySettings = webSocketFactory.getProxySettings();
			proxySettings.setHost(proxy.getHost());
			proxySettings.setPort(Integer.parseInt(proxy.getPort()));
			builder.setWebsocketFactory(webSocketFactory);
		}
		builder.build();
	}

}