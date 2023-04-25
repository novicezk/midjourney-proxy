package com.github.novicezk.midjourney.dto;

import lombok.Data;

@Data
public class UVTaskDTO {
	/**
	 * 自定义字符串, task中保留.
	 */
	private String state;
	/**
	 * content: id u1.
	 */
	private String content;
}
