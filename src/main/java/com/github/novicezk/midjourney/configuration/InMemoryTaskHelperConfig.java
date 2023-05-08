package com.github.novicezk.midjourney.configuration;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.support.task.InMemoryTaskHelper;
import com.github.novicezk.midjourney.support.task.TaskHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "mj.task-store.type", havingValue = "in-memory")
public class InMemoryTaskHelperConfig {
    @Bean
    TaskHelper taskHelper(ProxyProperties properties) {
        return new InMemoryTaskHelper(properties);
    }
}
