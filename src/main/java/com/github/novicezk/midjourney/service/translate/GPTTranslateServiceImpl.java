package com.github.novicezk.midjourney.service.translate;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.TranslateService;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import static com.theokanning.openai.service.OpenAiService.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofSeconds;

@Slf4j
public class GPTTranslateServiceImpl implements TranslateService {
	private final OpenAiService openAiService;
	private final ProxyProperties.OpenaiConfig openaiConfig;

	public GPTTranslateServiceImpl(ProxyProperties.OpenaiConfig openaiConfig) {
		if (CharSequenceUtil.isBlank(openaiConfig.getGptApiKey())) {
			throw new BeanDefinitionValidationException("mj-proxy.openai.gpt-api-key未配置");
		}
		this.openaiConfig = openaiConfig;

		ObjectMapper mapper = defaultObjectMapper();
		OkHttpClient client = defaultClient(openaiConfig.getGptApiKey(), openaiConfig.getTimeout())
			.newBuilder()
			.build();

		String BASE_URL = openaiConfig.getGptApiUrl();
		if (CharSequenceUtil.isBlank(BASE_URL)) {
			BASE_URL = "https://api.openai.com/";
		}

		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl(BASE_URL)
			.client(client)
			.addConverterFactory(JacksonConverterFactory.create(mapper))
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build();

		OpenAiApi api = retrofit.create(OpenAiApi.class);
		this.openAiService = new OpenAiService(api, client.dispatcher().executorService());
	}

	@Override
	public String translateToEnglish(String prompt) {
		if (!containsChinese(prompt)) {
			return prompt;
		}
		ChatMessage m1 = new ChatMessage("system", "把中文翻译成英文");
		ChatMessage m2 = new ChatMessage("user", prompt);
		ChatCompletionRequest request = ChatCompletionRequest.builder()
				.model(this.openaiConfig.getModel())
				.temperature(this.openaiConfig.getTemperature())
				.maxTokens(this.openaiConfig.getMaxTokens())
				.messages(List.of(m1, m2))
				.build();
		try {
			List<ChatCompletionChoice> choices = this.openAiService.createChatCompletion(request).getChoices();
			if (!choices.isEmpty()) {
				return choices.get(0).getMessage().getContent();
			}
		} catch (Exception e) {
			log.warn("调用chat-gpt接口翻译中文失败: {}", e.getMessage());
		}
		return prompt;
	}
}