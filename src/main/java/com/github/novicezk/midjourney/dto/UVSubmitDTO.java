package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("提交UV任务参数")
public class UVSubmitDTO {
	/**
	 * state: 自定义参数, task中保留.
	 */
	@ApiModelProperty("自定义参数, task中保留")
	private String state;
	/**
	 * content: id u1.
	 */
	@ApiModelProperty(value = "任务描述: 如 1320098173412546 U2", required = true)
	private String content;
	/**
	 * notifyHook of caller
	 */
	@ApiModelProperty("回调地址")
	private String notifyHook;
}
