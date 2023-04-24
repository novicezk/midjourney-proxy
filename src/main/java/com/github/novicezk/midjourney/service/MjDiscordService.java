package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.result.Message;

public interface MjDiscordService {

	Message<Void> imagine(String prompt);

	Message<Void> up(String messageId, String action, int index, String messageHash);

}