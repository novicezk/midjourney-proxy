package com.github.novicezk.midjourney.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ApiModel("任务")
public class Task implements Serializable {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	private Action action;
	@ApiModelProperty("任务ID")
	private String id;
	@ApiModelProperty("提示词")
	private String prompt;
	@ApiModelProperty("提示词-英文")
	private String promptEn;

	@ApiModelProperty("任务描述")
	private String description;
	@ApiModelProperty("自定义参数")
	private String state;
	@ApiModelProperty("提交时间")
	private Long submitTime;
	@ApiModelProperty("开始执行时间")
	private Long startTime;
	@ApiModelProperty("结束时间")
	private Long finishTime;
	@ApiModelProperty("图片url")
	private String imageUrl;
	private TaskStatus status = TaskStatus.NOT_START;
	@ApiModelProperty("失败原因")
	private String failReason;

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

	@JsonIgnore
	private final transient Object lock = new Object();

	public void sleep() throws InterruptedException {
		synchronized (this.lock) {
			this.lock.wait();
		}
	}

	public void awake() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}

}
