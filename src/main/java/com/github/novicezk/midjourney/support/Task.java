package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.domain.DomainObject;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("任务")
public class Task extends DomainObject {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	@ApiModelProperty("Task type")
	private TaskAction action;
	@ApiModelProperty("任务状态")
	private TaskStatus status = TaskStatus.NOT_START;

	@ApiModelProperty("prompt word")
	private String prompt;
	@ApiModelProperty("Prompt word-English")
	private String promptEn;

	@ApiModelProperty("Task Description")
	private String description;
	@ApiModelProperty("custom parameters")
	private String state;

	@ApiModelProperty("Submission time")
	private Long submitTime;
	@ApiModelProperty("Start execution time")
	private Long startTime;
	@ApiModelProperty("End time")
	private Long finishTime;

	@ApiModelProperty("image url")
	private String imageUrl;

	@ApiModelProperty("Task Progress")
	private String progress;
	@ApiModelProperty("Failure reason")
	private String failReason;

	public void start() {
		this.startTime = System.currentTimeMillis();
		this.status = TaskStatus.SUBMITTED;
		this.progress = "0%";
	}

	public void success() {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.SUCCESS;
		this.progress = "100%";
	}

	public void fail(String reason) {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.FAILURE;
		this.failReason = reason;
		this.progress = "";
	}
}
