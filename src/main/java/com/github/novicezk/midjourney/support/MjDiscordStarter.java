package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.MjDiscordProperties;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MjDiscordStarter implements ApplicationListener<ApplicationStartedEvent> {
	@Resource
	private MjDiscordProperties properties;
	@Resource
	private DiscordMessageListener discordMessageListener;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.properties.getBotToken(),
				GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
		builder.addEventListeners(this.discordMessageListener);
		builder.build();
	}

}