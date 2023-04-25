package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import lombok.Data;

import java.util.Date;

@Data
public class MjTask {

	private Action action;
	private String id;
	private String prompt;
	private String description;
	private String state;
	private Date submitDate;
	private Date finishDate;
	private String messageId;
	private String messageHash;
	private String imageUrl;

	private TaskStatus status = TaskStatus.NOT_START;

}
