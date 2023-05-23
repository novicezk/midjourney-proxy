package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.Action;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("提交Imagine或UV任务参数")
public class SubmitDTO {
	/**
	 * state: 自定义参数, task中保留.
	 */
	@ApiModelProperty("自定义参数, task中保留")
	private String state;
	/**
	 * 动作: IMAGINE\UPSCALE\VARIATION\RESET.
	 */
	@ApiModelProperty(value = "IMAGINE:出图；UPSCALE:选中放大；VARIATION：选中其中的一张图，生成四张相似的", required = true)
	private Action action;
	/**
	 * prompt: action 为 IMAGINE 必传.
	 */
	@ApiModelProperty("提示词: action 为 IMAGINE 必传")
	private String prompt;
	/**
	 * 任务ID: action 为 UPSCALE\VARIATION\RESET 必传.
	 */
	@ApiModelProperty("任务ID: action 为 UPSCALE,VARIATION 必传")
	private String taskId;
	/**
	 * index: action 为 UPSCALE\VARIATION 必传.
	 */
	@ApiModelProperty("序号: action 为 UPSCALE,VARIATION 必传")
	private Integer index;
	/**
	 * notifyHook of caller
	 */
	@ApiModelProperty("回调地址")
	private String notifyHook;
}
