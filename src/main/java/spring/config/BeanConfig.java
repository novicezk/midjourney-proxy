package spring.config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.codecs.ObjectCodec;
import com.github.novicezk.midjourney.loadbalancer.rule.IRule;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.store.InMemoryTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.store.MongoTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.store.RedisTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.NoTranslateServiceImpl;
import com.github.novicezk.midjourney.support.DiscordAccountHelper;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.internal.MongoClientImpl;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BeanConfig {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private ProxyProperties properties;


	@Value("${spring.data.mongodb.uri}")
	String monogDBConnectionString;

	@Bean
	TranslateService translateService() {
		return switch (this.properties.getTranslateWay()) {
			case BAIDU -> new BaiduTranslateServiceImpl(this.properties.getBaiduTranslate());
			case GPT -> new GPTTranslateServiceImpl(this.properties);
			default -> new NoTranslateServiceImpl();
		};
	}

	public MongoClient mongoClient() {
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new ObjectCodec())
		);

		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(monogDBConnectionString))
				.codecRegistry(codecRegistry)
				.build();
		return MongoClients.create(settings);
	}

	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoClient(),"minis-content-generation");
	}
	@Bean
	TaskStoreService taskStoreService(RedisConnectionFactory redisConnectionFactory) {
		ProxyProperties.TaskStore.Type type = this.properties.getTaskStore().getType();
		Duration timeout = this.properties.getTaskStore().getTimeout();
		CodecRegistry registry = CodecRegistries.fromRegistries(
				CodecRegistries.fromCodecs(new ObjectCodec()), // the codec you need to implement
				MongoClientSettings.getDefaultCodecRegistry());

		return switch (type) {
			case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
			case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
			case MONGODB ->
					new MongoTaskStoreServiceImpl(mongoTemplate());
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
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public IRule loadBalancerRule() {
		String ruleClassName = IRule.class.getPackageName() + "." + this.properties.getAccountChooseRule();
		return ReflectUtil.newInstance(ruleClassName);
	}

	@Bean
	List<MessageHandler> messageHandlers() {
		return this.applicationContext.getBeansOfType(MessageHandler.class).values().stream().toList();
	}

	@Bean
	DiscordAccountHelper discordAccountHelper(DiscordHelper discordHelper, TaskStoreService taskStoreService, NotifyService notifyService) throws IOException {
		var resources = this.applicationContext.getResources("classpath:api-params/*.json");
		Map<String, String> paramsMap = new HashMap<>();
		for (var resource : resources) {
			String filename = resource.getFilename();
			String params = IoUtil.readUtf8(resource.getInputStream());
			paramsMap.put(filename.substring(0, filename.length() - 5), params);
		}
		return new DiscordAccountHelper(discordHelper, this.properties, restTemplate(), taskStoreService, notifyService, messageHandlers(), paramsMap);
	}


}
