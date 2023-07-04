package spring.config;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.store.InMemoryTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.store.RedisTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskMixin;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.github.novicezk.midjourney.wss.bot.BotMessageListener;
import com.github.novicezk.midjourney.wss.bot.BotWebSocketStarter;
import com.github.novicezk.midjourney.wss.user.UserMessageListener;
import com.github.novicezk.midjourney.wss.user.UserWebSocketStarter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class BeanConfig {

	@Bean
	TranslateService translateService(ProxyProperties properties) {
		return switch (properties.getTranslateWay()) {
			case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
			case GPT -> new GPTTranslateServiceImpl(properties);
			default -> prompt -> prompt;
		};
	}

	@Bean
	TaskStoreService taskStoreService(ProxyProperties proxyProperties, RedisConnectionFactory redisConnectionFactory) {
		ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
		Duration timeout = proxyProperties.getTaskStore().getTimeout();
		return switch (type) {
			case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
			case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
		};
	}

	@Bean
	RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Task.class));
		return redisTemplate;
	}

	@Bean
	WebSocketStarter webSocketStarter(ProxyProperties properties) {
		return properties.getDiscord().isUserWss() ? new UserWebSocketStarter(properties) : new BotWebSocketStarter(properties);
	}

	@Bean
	@ConditionalOnProperty(prefix = "mj.discord", name = "user-wss", havingValue = "true")
	UserMessageListener userMessageListener() {
		return new UserMessageListener();
	}

	@Bean
	@ConditionalOnProperty(prefix = "mj.discord", name = "user-wss", havingValue = "false")
	BotMessageListener botMessageListener() {
		return new BotMessageListener();
	}

	@Bean
	ApplicationRunner enableMetaChangeReceiverInitializer(WebSocketStarter webSocketStarter) {
		return args -> webSocketStarter.start();
	}

	@Bean
	Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(ProxyProperties properties) {
		if (properties.isIncludeTaskExtended()) {
			return builder -> {
			};
		}
		return builder -> builder.mixIn(Task.class, TaskMixin.class);
	}

}
