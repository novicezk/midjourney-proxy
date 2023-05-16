package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.Action;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("出图模型")
public class SubmitDTO {
	/**
	 * state: 自定义参数, task中保留.
	 */
	@ApiModelProperty("自定义参数, task中保留")
	private String state;
	/**
	 * 动作: IMAGINE\UPSCALE\VARIATION\RESET.
	 */
	@ApiModelProperty("IMAGINE:出图；UPSCALE:选中放大；VARIATION：选中其中的一张图，生成四张相似的；RESET：重新生成")
	private Action action;
	/**
	 * prompt: action 为 IMAGINE 必传.
	 */
	@ApiModelProperty("命令，action 为 IMAGINE 必传")
	private String prompt;
	/**
	 * 任务ID: action 为 UPSCALE\VARIATION\RESET 必传.
	 */
	@ApiModelProperty("UPSCALE；VARIATION；RESET")
	private String taskId;
	/**
	 * index: action 为 UPSCALE\VARIATION 必传.
	 */
	@ApiModelProperty("为 UPSCALE；VARIATION 必传")
	private Integer index;
	/**
	 * notifyHook of caller
	 */
	@ApiModelProperty("回调地址")
	private String notifyHook;
}
