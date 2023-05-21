package spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

	@Bean
	public Docket defaultApi2() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(new ApiInfoBuilder()
						.contact(new Contact("novicezk", "https://github.com/novicezk", "zhukai_novice@163.com"))
						.title("Midjourney Proxy API文档")
						.description("提交Midjourney任务，接收任务结果")
						.termsOfServiceUrl("https://github.com/novicezk/midjourney-proxy")
						.version("1.x")
						.build())
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.github.novicezk.midjourney.controller"))
				.paths(PathSelectors.any())
				.build();
	}
}