package com.github.novicezk.midjourney.loadbalancer.rule;

import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;

import java.util.List;

/**
 * 最少等待空闲.
 * 选择等待数最少的实例，如果都不需要等待，则选择空闲数最多的实例
 */
public class BestWaitIdleRule implements IRule {

	@Override
	public DiscordInstance choose(List<DiscordInstance> instances) {
		if (instances.isEmpty()) {
			return null;
		}
		// 核心线程空闲最多的 账户
		DiscordInstance discordInstance = instances.stream().filter(e -> {
			return e.account().getCoreSize() - e.getThreadActiveCount() > 0;
		}).max((i1, i2) -> {
			int wait1 = i1.account().getCoreSize() - i1.getThreadActiveCount();
			int wait2 = i2.account().getCoreSize() - i2.getThreadActiveCount();
			return wait1 - wait2;
		}).orElse(null);

		if(null != discordInstance){
			return discordInstance;
		}
		// 获取剩余任务队列大小最大的 账户
		return instances.stream().max((i1, i2) -> {
			int wait1 = i1.account().getQueueSize() - i1.getTaskQueueSize();
			int wait2 = i2.account().getQueueSize() - i2.getTaskQueueSize();
			return wait1 - wait2;
		}).orElse(null);
	}

}
