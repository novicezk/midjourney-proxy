package com.github.novicezk.midjourney.dto;

import lombok.Data;

@Data
public class UVTaskDTO {
	/**
	 * 自定义参数, task中保留.
	 */
	private String state;
	/**
	 * content: id u1.
	 */
	private String content;
}
