package com.github.novicezk.midjourney.wss.handle;

import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.util.ConvertUtils;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * upscale消息处理.
 * 完成(create): **cat** - Upscaled (Beta\Light\Creative等) by <@1083152202048217169> (fast)
 * 完成(create): **cat** - Upscaled by <@1083152202048217169> (fast)
 * 完成(create): **cat** - Image #1 <@1012983546824114217>
 */
@Component
public class UpscaleSuccessHandler extends MessageHandler {
	private static final String CONTENT_REGEX_1 = "\\*\\*(.*?)\\*\\* - Upscaled \\(.*?\\) by <@\\d+> \\((.*?)\\)";
	private static final String CONTENT_REGEX_2 = "\\*\\*(.*?)\\*\\* - Upscaled by <@\\d+> \\((.*?)\\)";
	private static final String CONTENT_REGEX_3 = "\\*\\*(.*?)\\*\\* - Image #\\d <@\\d+>";

	@Override
	public void handle(DiscordInstance instance, MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		ContentParseData parseData = getParseData(content);
		if (MessageType.CREATE.equals(messageType) && parseData != null && hasImage(message)) {
			TaskCondition condition = new TaskCondition()
					.setActionSet(Set.of(TaskAction.UPSCALE))
					.setFinalPromptEn(parseData.getPrompt());
			findAndFinishImageTask(instance, condition, parseData.getPrompt(), message);
		}
	}

	private ContentParseData getParseData(String content) {
		ContentParseData parseData = ConvertUtils.parseContent(content, CONTENT_REGEX_1);
		if (parseData == null) {
			parseData = ConvertUtils.parseContent(content, CONTENT_REGEX_2);
		}
		if (parseData != null) {
			return parseData;
		}
		Matcher matcher = Pattern.compile(CONTENT_REGEX_3).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		parseData = new ContentParseData();
		parseData.setPrompt(matcher.group(1));
		parseData.setStatus("done");
		return parseData;
	}

}
