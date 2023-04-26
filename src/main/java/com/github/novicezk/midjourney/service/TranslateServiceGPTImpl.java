package com.github.novicezk.midjourney.service;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateServiceGPTImpl implements TranslateService {
	private final ProxyProperties properties;

	private OpenAiService openAiService;

	@PostConstruct
	void init() {
		if (CharSequenceUtil.isNotBlank(this.properties.getOpenai().getGptApiKey())) {
			this.openAiService = new OpenAiService(this.properties.getOpenai().getGptApiKey(), this.properties.getOpenai().getTimeout());
		}
	}

	@Override
	public String translateToEnglish(String prompt) {
		if (!containsChinese(prompt) || this.openAiService == null) {
			return prompt;
		}
		ChatMessage m1 = new ChatMessage("system", "把中文翻译成英文");
		ChatMessage m2 = new ChatMessage("user", prompt);
		ChatCompletionRequest request = ChatCompletionRequest.builder()
				.model(this.properties.getOpenai().getModel())
				.temperature(this.properties.getOpenai().getTemperature())
				.maxTokens(this.properties.getOpenai().getMaxTokens())
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