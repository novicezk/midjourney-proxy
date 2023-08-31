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
		return instances.stream().min((i1, i2) -> {
			int wait1 = i1.getRunningFutures().size() - i1.account().getCoreSize();
			int wait2 = i2.getRunningFutures().size() - i2.account().getCoreSize();
			if (wait1 == wait2 && wait1 == 0) {
				// 都不需要等待时，选择空闲数最多的
				int idle1 = i1.account().getCoreSize() - i1.getRunningTasks().size();
				int idle2 = i2.account().getCoreSize() - i2.getRunningTasks().size();
				return idle2 - idle1;
			}
			return wait1 - wait2;
		}).orElse(null);
	}

}
