package com.github.novicezk.midjourney.service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/6/28
 **/
public interface LoadBalancerService {

    AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    /**
     * 简单的自增
     *
     * @return int
     */
    default int getAndIncrement() {
        int current;
        int next;
        do {
            current = ATOMIC_INTEGER.get();
            next = current == Integer.MAX_VALUE ? 0 : current + 1;
        } while (!ATOMIC_INTEGER.compareAndSet(current, next));
        return next;
    }

    /**
     * 获取轮询的key
     *
     * @return String
     */
    String getLoadBalancerKey();
}
