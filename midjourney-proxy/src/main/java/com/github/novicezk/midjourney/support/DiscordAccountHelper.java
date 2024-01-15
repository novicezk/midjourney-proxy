package com.github.novicezk.midjourney.support;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.domain.DiscordAccount;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstanceImpl;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import com.github.novicezk.midjourney.wss.user.UserMessageListener;
import com.github.novicezk.midjourney.wss.user.UserWebSocketStarter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DiscordAccountHelper {
	private final DiscordHelper discordHelper;
	private final ProxyProperties properties;
	private final RestTemplate restTemplate;
	private final TaskStoreService taskStoreService;
	private final NotifyService notifyService;
	private final List<MessageHandler> messageHandlers;
	private final Map<String, String> paramsMap;

	public DiscordInstance createDiscordInstance(DiscordAccount account) {
		if (!CharSequenceUtil.isAllNotBlank(account.getGuildId(), account.getChannelId(), account.getUserToken())) {
			throw new IllegalArgumentException("guildId, channelId, userToken must not be blank");
		}
		if (CharSequenceUtil.isBlank(account.getUserAgent())) {
			account.setUserAgent(Constants.DEFAULT_DISCORD_USER_AGENT);
		}
		var messageListener = new UserMessageListener(account, this.messageHandlers);
		var webSocketStarter = new UserWebSocketStarter(this.discordHelper.getWss(), account, messageListener, this.properties.getProxy());
		return new DiscordInstanceImpl(account, webSocketStarter, this.restTemplate,
				this.taskStoreService, this.notifyService, this.paramsMap);
	}
}
