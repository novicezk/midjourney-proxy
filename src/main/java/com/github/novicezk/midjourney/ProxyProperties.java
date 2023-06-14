package com.github.novicezk.midjourney;

import com.github.novicezk.midjourney.enums.TranslateWay;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "mj")
public class ProxyProperties {
	/**
	 * task存储配置.
	 */
	private final TaskStore taskStore = new TaskStore();
	/**
	 * discord配置.
	 */
	private final DiscordConfig discord = new DiscordConfig();
	/**
	 * 代理配置.
	 */
	private final ProxyConfig proxy = new ProxyConfig();
	/**
	 * 反代配置.
	 */
	private final NgDiscordConfig ngDiscord = new NgDiscordConfig();
	/**
	 * 任务队列配置.
	 */
	private final TaskQueueConfig queue = new TaskQueueConfig();
	/**
	 * 百度翻译配置.
	 */
	private final BaiduTranslateConfig baiduTranslate = new BaiduTranslateConfig();
	/**
	 * openai配置.
	 */
	private final OpenaiConfig openai = new OpenaiConfig();
	/**
	 * 中文prompt翻译方式.
	 */
	private TranslateWay translateWay = TranslateWay.NULL;
	/**
	 * 接口密钥，为空不启用鉴权；调用接口时需要加请求头 mj-api-secret.
	 */
	private String apiSecret;
	/**
	 * 任务状态变更回调地址.
	 */
	private String notifyHook;

	@Data
	public static class DiscordConfig {
		/**
		 * 你的服务器id.
		 */
		private String guildId;
		/**
		 * 你的频道id.
		 */
		private String channelId;
		/**
		 * 你的登录token.
		 */
		private String userToken;
		/**
		 * 是否使用user_token连接wss，默认启用.
		 */
		private boolean userWss = true;
		/**
		 * 调用discord接口、连接wss时的user-agent.
		 */
		private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";
		/**
		 * 你的机器人token.
		 */
		private String botToken;
		/**
		 * prompt拼接ID的前缀，可使用多个字符，不要使用 '\' 字符.
		 */
		private String idPrefix = "[";
		/**
		 * prompt拼接ID的后缀，可使用多个字符，不要使用 '\' 字符.
		 */
		private String idSuffix = "]";
	}

	@Data
	public static class BaiduTranslateConfig {
		/**
		 * 百度翻译的APP_ID.
		 */
		private String appid;
		/**
		 * 百度翻译的密钥.
		 */
		private String appSecret;
	}

	@Data
	public static class OpenaiConfig {
		/**
		 * gpt的api-key.
		 */
		private String gptApiKey;
		/**
		 * 超时时间.
		 */
		private Duration timeout = Duration.ofSeconds(30);
		/**
		 * 使用的模型.
		 */
		private String model = "gpt-3.5-turbo";
		/**
		 * 返回结果的最大分词数.
		 */
		private int maxTokens = 2048;
		/**
		 * 相似度，取值 0-2.
		 */
		private double temperature = 0;
	}

	@Data
	public static class TaskStore {
		/**
		 * 任务过期时间，默认30天.
		 */
		private Duration timeout = Duration.ofDays(30);
		/**
		 * 任务存储方式: redis(默认)、in_memory.
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
		 * 代理host.
		 */
		private String host;
		/**
		 * 代理端口.
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
	}

	@Data
	public static class TaskQueueConfig {
		/**
		 * 并发数.
		 */
		private int coreSize = 3;
		/**
		 * 等待队列长度.
		 */
		private int queueSize = 10;
		/**
		 * 任务超时时间(分钟).
		 */
		private int timeoutMinutes = 5;
	}
}
