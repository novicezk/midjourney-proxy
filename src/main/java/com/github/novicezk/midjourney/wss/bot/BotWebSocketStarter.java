package com.github.novicezk.midjourney.wss.bot;

import javax.annotation.Resource;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.neovisionaries.ws.client.WebSocketFactory;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestConfig;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

public class BotWebSocketStarter implements WebSocketStarter {
    @Resource
    private BotMessageListener botMessageListener;

    private final ProxyProperties properties;

    private String discordApiUrl;

    public BotWebSocketStarter(ProxyProperties properties) {
        this.properties = properties;
        String baseUrl = this.properties.getNg().getHttps();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        this.discordApiUrl = baseUrl + "api/v10/";
        initProxy(properties);
    }

    @Override
    public void start() throws Exception {
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(
            this.properties.getDiscord().getBotToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        builder.addEventListeners(this.botMessageListener);
        WebSocketFactory webSocketFactory = createWebSocketFactory(this.properties);
        builder.setWebsocketFactory(webSocketFactory);
        builder.setSessionController(new CustomSessionController(this.properties.getNg().getWss()));
        builder.setRestConfigProvider(value -> new RestConfig().setBaseUrl(this.discordApiUrl));
        builder.build();
    }
}
