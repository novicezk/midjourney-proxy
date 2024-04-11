package com.github.novicezk.midjourney;

import com.github.novicezk.midjourney.enums.TranslateWay;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "mj")
public class ProxyProperties {
	/**
	 * task storage configuration.
	 */
	private final TaskStore taskStore = new TaskStore();
	/**
	 * discord account selection rules.
	 */
	private String accountChooseRule = "BestWaitIdleRule";
	/**
	 * discord single account configuration.
	 */
	private final DiscordAccountConfig discord = new DiscordAccountConfig();
	/**
	 * discord account pool configuration.
	 */
	private final List<DiscordAccountConfig> accounts = new ArrayList<>();
	/**
	 * Agent configuration.
	 */
	private final ProxyConfig proxy = new ProxyConfig();
	/**
	 * Anti-generation configuration.
	 */
	private final NgDiscordConfig ngDiscord = new NgDiscordConfig();
	/**
	 * Baidu translation configuration.
	 */
	private final BaiduTranslateConfig baiduTranslate = new BaiduTranslateConfig();
	/**
	 * openai configuration.
	 */
	private final OpenaiConfig openai = new OpenaiConfig();
	/**
	 * Chinese prompt translation method.
	 */
	private TranslateWay translateWay = TranslateWay.NULL;
	/**
	 * Interface key, if empty, authentication will not be enabled; when calling the interface, you need to add the request header mj-api-secret.
	 */
	private String apiSecret;
	/**
	 * Task status change callback address.
	 */
	private String notifyHook;
	/**
	 * Notification callback thread pool size.
	 */
	private int notifyPoolSize = 10;

	@Data
	public static class DiscordAccountConfig {
		/**
		 * 服务器ID.
		 */
		private String guildId;
		/**
		 * Channel ID.
		 */
		private String channelId;
		/**
		 * UserToken.
		 */
		private String userToken;
		/**
		 * UserUserAgent.
		 */
		private String userAgent = Constants.DEFAULT_DISCORD_USER_AGENT;
		/**
		 * it's usable or not.
		 */
		private boolean enable = true;
		/**
		 * Number of concurrencies.
		 */
		private int coreSize = 1;
		/**
		 * Waiting queue length.
		 */
		private int queueSize = 10;
		/**
		 * Task timeout (minutes).
		 */
		private int timeoutMinutes = 5;
	}

	@Data
	public static class BaiduTranslateConfig {
		/**
		 * Baidu Translation APP_ID.
		 */
		private String appid;
		/**
		 * Baidu's translation key.
		 */
		private String appSecret;
	}

	@Data
	public static class OpenaiConfig {
		/**
		 * Customize the api-url of gpt.
		 */
		private String gptApiUrl;
		/**
		 * api-key of gpt.
		 */
		private String gptApiKey;
		/**
		 * overtime time.
		 */
		private Duration timeout = Duration.ofSeconds(30);
		/**
		 * Model used.
		 */
		private String model = "gpt-3.5-turbo";
		/**
		 * The maximum number of words returned in the result.
		 */
		private int maxTokens = 2048;
		/**
		 * Similarity, value 0-2.
		 */
		private double temperature = 0;
	}

	@Data
	public static class TaskStore {
		/**
		 * Task expiration time, default 30 days.
		 */
		private Duration timeout = Duration.ofDays(30);
		/**
		 * Task storage method: redis (default), in_memory.
		 */
		private Type type = Type.IN_MEMORY;

		public enum Type {
			/**
			 * redis.
			 */
			REDIS,
			/**
			 * in_memory.
			 */
			IN_MEMORY
		}
	}

	@Data
	public static class ProxyConfig {
		/**
		 * Proxy host.
		 */
		private String host;
		/**
		 * proxy port.
		 */
		private Integer port;
	}

	@Data
	public static class NgDiscordConfig {
		/**
		 * https://discord.com 反代.
		 */
		private String server;
		/**
		 * https://cdn.discordapp.com 反代.
		 */
		private String cdn;
		/**
		 * wss://gateway.discord.gg 反代.
		 */
		private String wss;
		/**
		 * wss://gateway-us-east1-b.discord.gg 反代.
		 */
		private String resumeWss;
		/**
		 * https://discord-attachments-uploads-prd.storage.googleapis.com 反代.
		 */
		private String uploadServer;
	}

	@Data
	public static class TaskQueueConfig {
		/**
		 * Number of concurrencies.
		 */
		private int coreSize = 1;
		/**
		 * Waiting queue length.
		 */
		private int queueSize = 10;
		/**
		 * Task timeout (minutes).
		 */
		private int timeoutMinutes = 5;
	}
}
