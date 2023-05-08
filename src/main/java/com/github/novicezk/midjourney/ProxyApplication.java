package com.github.novicezk.midjourney;

import com.github.novicezk.midjourney.enums.TaskStore;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

}
