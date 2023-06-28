package spring.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.support.ApiAuthorizeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiAuthorizeInterceptor apiAuthorizeInterceptor;

    private final ProxyProperties properties;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:doc.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (CharSequenceUtil.isNotBlank(this.properties.getApiSecret())) {
            registry.addInterceptor(this.apiAuthorizeInterceptor)
                    .addPathPatterns("/submit/**", "/task/**");
        }
    }

}
