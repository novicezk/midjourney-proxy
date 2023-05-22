package com.github.novicezk.midjourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import spring.config.BeanConfig;
import spring.config.Knife4jConfig;

@EnableScheduling
@SpringBootApplication
@Import({BeanConfig.class, Knife4jConfig.class})
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

}
