package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.TaskType;
import lombok.Data;

@Data
public class TaskDTO {
	/**
	 * 微信群名.
	 */
	private String room;
	/**
	 * 微信名.
	 */
	private String user;

	/**
	 * 任务类型.
	 */
	private TaskType type;
	/**
	 * prompt.
	 */
	private String prompt;

}
