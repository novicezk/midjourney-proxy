package com.github.novicezk.midjourney.wss.bot;

import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Getter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestConfig;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

public class BotWebSocketStarter implements WebSocketStarter {
    private final BotMessageListener botMessageListener;
    private final DiscordHelper discordHelper;
    private final String host;
    private final Integer port;

    @Getter
    private final String botToken;

    public BotWebSocketStarter(String host, Integer port, String botToken, BotMessageListener botMessageListener, DiscordHelper discordHelper) {
        initProxy(host, port);
        this.host = host;
        this.port = port;
        this.botToken = botToken;
        this.botMessageListener = botMessageListener;
        this.discordHelper = discordHelper;
    }

    @Override
    public void start() throws Exception {
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(botToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        builder.addEventListeners(this.botMessageListener);
        WebSocketFactory webSocketFactory = createWebSocketFactory(host, port);
        builder.setWebsocketFactory(webSocketFactory);
        builder.setSessionController(new CustomSessionController(this.discordHelper.getWss()));
        builder.setRestConfigProvider(value -> new RestConfig().setBaseUrl(this.discordHelper.getServer() + "/api/v10/"));
        builder.build();
    }
}
