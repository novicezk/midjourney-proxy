package com.github.novicezk.midjourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import spring.config.BeanConfig;

@EnableScheduling
@SpringBootApplication
@Import(BeanConfig.class)
public class ProxyApplication {

	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

}
