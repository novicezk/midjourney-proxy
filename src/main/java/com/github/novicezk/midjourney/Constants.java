package com.github.novicezk.midjourney;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Constants {
	// 任务扩展属性 start
	public static final String TASK_PROPERTY_NOTIFY_HOOK = "notifyHook";
	public static final String TASK_PROPERTY_FINAL_PROMPT = "finalPrompt";
	public static final String TASK_PROPERTY_RELATED_TASK_ID = "relatedTaskId";
	public static final String TASK_PROPERTY_MESSAGE_ID = "messageId";
	public static final String TASK_PROPERTY_PROGRESS_MESSAGE_ID = "progressMessageId";
	public static final String TASK_PROPERTY_FLAGS = "flags";
	public static final String TASK_PROPERTY_MESSAGE_HASH = "messageHash";
	// 任务扩展属性 end

	public static final String API_SECRET_HEADER_NAME = "mj-api-secret";
}
