package com.github.novicezk.midjourney.loadbalancer.rule;

import cn.hutool.core.util.RandomUtil;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wait for at least idle time.
 * Select the instance with the least waiting number. If there is no need to wait, select it randomly.
 * */
public class BestWaitIdleRule implements IRule {

	@Override
	public DiscordInstance choose(List<DiscordInstance> instances) {
		if (instances.isEmpty()) {
			return null;
		}
		Map<Integer, List<DiscordInstance>> map = instances.stream()
				.collect(Collectors.groupingBy(i -> {
					int wait = i.getRunningFutures().size() - i.account().getCoreSize();
					return wait >= 0 ? wait : -1;
				}));
		List<DiscordInstance> instanceList = map.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getKey)).orElseThrow().getValue();
		return RandomUtil.randomEle(instanceList);
	}

}
