package com.github.novicezk.midjourney.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Task implements Serializable {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	private Action action;
	private String id;
	private String prompt;
	private String promptEn;

	private String description;
	private String state;
	private Long submitTime;
	private Long finishTime;
	private String imageUrl;
	private TaskStatus status = TaskStatus.NOT_START;

	// Hidden -- start
	@JsonIgnore
	private String key;
	@JsonIgnore
	private String finalPrompt;
	@JsonIgnore
	private String notifyHook;
	@JsonIgnore
	private String relatedTaskId;
	@JsonIgnore
	private String messageId;
	@JsonIgnore
	private String messageHash;
	// Hidden -- end
}

