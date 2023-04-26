package com.github.novicezk.midjourney.service.translate;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.TranslateService;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.util.List;

@Slf4j
public class GPTTranslateServiceImpl implements TranslateService {
	private final OpenAiService openAiService;
	private final ProxyProperties.OpenaiConfig openaiConfig;

	public GPTTranslateServiceImpl(ProxyProperties.OpenaiConfig openaiConfig) {
		if (CharSequenceUtil.isBlank(openaiConfig.getGptApiKey())) {
			throw new BeanDefinitionValidationException("mj-proxy.openai.gpt-api-key未配置");
		}
		this.openaiConfig = openaiConfig;
		this.openAiService = new OpenAiService(openaiConfig.getGptApiKey(), openaiConfig.getTimeout());
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