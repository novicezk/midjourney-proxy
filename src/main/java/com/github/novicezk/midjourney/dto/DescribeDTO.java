package com.github.novicezk.midjourney.dto;

import lombok.Data;

@Data
public class DescribeDTO {
	/**
	 * 自定义参数.
	 */
	private String state;
	/**
	 * 文件base64: data:image/png;base64,xxx.
	 */
	private String base64;
	/**
	 * notifyHook of caller.
	 */
	private String notifyHook;
}
