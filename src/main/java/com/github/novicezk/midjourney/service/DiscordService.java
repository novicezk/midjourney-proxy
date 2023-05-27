package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.result.Message;
import eu.maxschuster.dataurl.DataUrl;

public interface DiscordService {

	Message<Void> imagine(String prompt);

	Message<Void> upscale(String messageId, int index, String messageHash);

	Message<Void> variation(String messageId, int index, String messageHash);

	Message<Void> reroll(String messageId, String messageHash);

	Message<String> upload(String fileName, DataUrl dataUrl);

	Message<Void> describe(String finalFileName);

}
