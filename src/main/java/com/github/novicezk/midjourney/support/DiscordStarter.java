package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.ProxyProperties;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
		builder.build();
	}

}