package spring.config;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.BotLoadBalancerServiceImpl;
import com.github.novicezk.midjourney.service.LoadBalancerService;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.github.novicezk.midjourney.wss.bot.BotMessageListener;
import com.github.novicezk.midjourney.wss.bot.BotWebSocketStarter;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/6/28
 **/
@Configuration
@EnableConfigurationProperties(ProxyProperties.class)
@ConditionalOnProperty(prefix = "mj.discord", name = "user-wss", havingValue = "false")
public class BotWebSocketStarterConfig {
    @Bean
    public Map<String, BotMessageListener> botMessageListener(ProxyProperties properties, List<MessageHandler> messageHandlerList) {
        return properties.getDiscord().getBotTokenConfigList().stream()
                .map(x -> new BotMessageListener(x.getChannelId(), messageHandlerList))
                .collect(Collectors.toMap(BotMessageListener::getChannelId, Function.identity()));
    }

    @Bean
    public Map<String, WebSocketStarter> botWebSocketStarter(ProxyProperties properties, Map<String, BotMessageListener> botMessageListenerMap, DiscordHelper discordHelper) {
        ProxyProperties.ProxyConfig proxy = properties.getProxy();
        return properties.getDiscord().getBotTokenConfigList().stream()
                .map(x -> new BotWebSocketStarter(proxy.getHost(), proxy.getPort(),
                        x.getBotToken(), botMessageListenerMap.get(x.getChannelId()), discordHelper))
                .collect(Collectors.toMap(BotWebSocketStarter::getBotToken, Function.identity()));

    }

    @Bean
    public LoadBalancerService botLoadBalancerService(ProxyProperties properties) {
        return new BotLoadBalancerServiceImpl(properties);
    }

}
