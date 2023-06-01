package com.github.novicezk.midjourney.wss.bot;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestConfig;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import javax.annotation.Resource;

public class BotWebSocketStarter implements WebSocketStarter {
	@Resource
	private BotMessageListener botMessageListener;
	@Resource
	private DiscordHelper discordHelper;

	private final ProxyProperties properties;

	public BotWebSocketStarter(ProxyProperties properties) {
		initProxy(properties);
		this.properties = properties;
	}

	@Override
	public void start() throws Exception {
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(
				this.properties.getDiscord().getBotToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
		builder.addEventListeners(this.botMessageListener);
		WebSocketFactory webSocketFactory = createWebSocketFactory(this.properties);
		builder.setWebsocketFactory(webSocketFactory);
		builder.setSessionController(new CustomSessionController(this.discordHelper.getWss()));
		builder.setRestConfigProvider(value -> new RestConfig().setBaseUrl(this.discordHelper.getServer() + "/api/v10/"));
		builder.build();
	}
}
