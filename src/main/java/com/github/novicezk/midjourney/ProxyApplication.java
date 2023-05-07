package com.github.novicezk.midjourney;

import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.support.task.InMemoryTaskHelper;
import com.github.novicezk.midjourney.support.task.RedisTaskHelper;
import com.github.novicezk.midjourney.support.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

@SpringBootApplication
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

	@Bean
	TranslateService translateService(ProxyProperties properties) {
		return switch (properties.getTranslateWay()) {
			case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
			case GPT -> new GPTTranslateServiceImpl(properties.getOpenai());
			default -> prompt -> prompt;
		};
	}

//	@Autowired
//	TaskHelper taskHelper;
//
//	@Bean
//	TaskHelper taskHelper(ProxyProperties properties) {
////		return Objects.equals(properties.getTaskStore(), "redis") ? new RedisTaskHelper() : new InMemoryTaskHelper();
////		return Objects.equals(properties.getTaskStore(), "redis") ? new RedisTaskHelper() : new InMemoryTaskHelper();
//		return taskHelper;
//	}

}
