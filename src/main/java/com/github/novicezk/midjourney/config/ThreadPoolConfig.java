package com.github.novicezk.midjourney.config;

import com.github.novicezk.midjourney.ProxyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Resource
    private ProxyProperties properties;

    @Bean("taskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        ProxyProperties.ThreadPoolConfig threadPoolConfig = properties.getThread();
        executor.setCorePoolSize(threadPoolConfig.getCore());
        executor.setMaxPoolSize(threadPoolConfig.getCore());
        executor.setQueueCapacity(threadPoolConfig.getQueue());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setThreadNamePrefix("ThreadPool-");
        executor.initialize();
        return executor;
    }
}
