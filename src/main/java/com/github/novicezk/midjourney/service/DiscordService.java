package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.result.Message;

public interface DiscordService {

	Message<Void> imagine(String prompt);

	Message<Void> upscale(String messageId, int index, String messageHash);

	Message<Void> variation(String messageId, int index, String messageHash);

	Message<Void> reset(String messageId, String messageHash);

}