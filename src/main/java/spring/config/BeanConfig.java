package spring.config;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.task.InMemoryTaskServiceImpl;
import com.github.novicezk.midjourney.service.task.RedisTaskServiceImpl;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.support.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class BeanConfig {

	@Bean
	TranslateService translateService(ProxyProperties properties) {
		return switch (properties.getTranslateWay()) {
			case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
			case GPT -> new GPTTranslateServiceImpl(properties.getOpenai());
			default -> prompt -> prompt;
		};
	}

	@Bean
	TaskService taskService(ProxyProperties proxyProperties, RedisConnectionFactory redisConnectionFactory) {
		ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
		Duration timeout = proxyProperties.getTaskStore().getTimeout();
		return switch (type) {
			case IN_MEMORY -> new InMemoryTaskServiceImpl(timeout);
			case REDIS -> new RedisTaskServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
		};
	}

	@Bean
	RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}

}
