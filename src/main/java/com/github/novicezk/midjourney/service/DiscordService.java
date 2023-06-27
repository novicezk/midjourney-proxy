package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.result.Message;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;

public interface DiscordService {

	Message<Void> imagine(String prompt);

	Message<Void> upscale(String messageId, int index, String messageHash, int messageFlags);

	Message<Void> variation(String messageId, int index, String messageHash, int messageFlags);

	Message<Void> reroll(String messageId, String messageHash, int messageFlags);

	Message<Void> describe(String finalFileName);

	Message<Void> blend(List<String> finalFileNames, BlendDimensions dimensions);

	Message<String> upload(String fileName, DataUrl dataUrl);

	Message<String> sendImageMessage(String content, String finalFileName);

}
