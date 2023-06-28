package spring.config;

import cn.hutool.core.io.resource.ResourceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.DiscordService;
import com.github.novicezk.midjourney.service.DiscordServiceImpl;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.store.InMemoryTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.store.RedisTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskMixin;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(ProxyProperties.class)
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Map<String, DiscordService> discordService(ProxyProperties properties, DiscordHelper discordHelper, RestTemplate restTemplate) {
        ProxyProperties.DiscordConfig discord = properties.getDiscord();
        String serverUrl = discordHelper.getServer();
        return discord.getDiscordAccountConfigList().stream().map(x -> new DiscordServiceImpl(x.getGuildId(), x.getChannelId(), x.getUserToken(),
                x.getSessionId(), discord.getUserAgent(), serverUrl + "/api/v9/interactions",
                serverUrl + "/api/v9/channels/" + x.getChannelId() + "/attachments",
                serverUrl + "/api/v9/channels/" + x.getChannelId() + "/messages",
                ResourceUtil.readUtf8Str("api-params/imagine.json"),
                ResourceUtil.readUtf8Str("api-params/upscale.json"),
                ResourceUtil.readUtf8Str("api-params/variation.json"),
                ResourceUtil.readUtf8Str("api-params/reroll.json"),
                ResourceUtil.readUtf8Str("api-params/describe.json"),
                ResourceUtil.readUtf8Str("api-params/blend.json"),
                ResourceUtil.readUtf8Str("api-params/message.json"), restTemplate)
        ).collect(Collectors.toMap(x -> x.getDiscordGuildId() + ":" + x.getDiscordChannelId(), Function.identity()));
    }

    @Bean
    public TranslateService translateService(ProxyProperties properties) {
        return switch (properties.getTranslateWay()) {
            case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
            case GPT -> new GPTTranslateServiceImpl(properties.getOpenai());
            default -> prompt -> prompt;
        };
    }

    @Bean
    public TaskStoreService taskStoreService(ProxyProperties proxyProperties, RedisConnectionFactory redisConnectionFactory) {
        ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
        Duration timeout = proxyProperties.getTaskStore().getTimeout();
        return switch (type) {
            case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
            case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
        };
    }

    @Bean
    public RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Task.class));
        return redisTemplate;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.mixIn(Task.class, TaskMixin.class);
    }

}
