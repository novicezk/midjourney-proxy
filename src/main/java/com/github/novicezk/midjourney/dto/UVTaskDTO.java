package com.github.novicezk.midjourney.dto;

import lombok.Data;

@Data
public class UVTaskDTO {
	/**
	 * state参数, 回调接口带回.
	 */
	private String state;
	/**
	 * content: id u1.
	 */
	private String content;
}
