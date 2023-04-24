package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.enums.TaskType;
import lombok.Data;

import java.util.Date;

@Data
public class MjTask {

	private TaskType type;
	private String key;
	private String prompt;
	private String room;
	private String user;
	private Date submitDate;
	private Date doneDate;
	private String messageId;
	private String messageHash;
	private boolean done = false;
	private String imageUrl;
	private boolean notifySuccess = false;

}
