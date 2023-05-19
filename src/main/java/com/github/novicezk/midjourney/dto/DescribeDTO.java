package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("图生文字模型")
public class DescribeDTO {
	/**
	 * 自定义参数.
	 */
	@ApiModelProperty("自定义参数")
	private String state;
	/**
	 * 文件base64: data:image/png;base64,xxx.
	 */
	@ApiModelProperty("图片base64")
	private String base64;
	/**
	 * notifyHook of caller.
	 */
	@ApiModelProperty("回调地址")
	private String notifyHook;
}
