package com.github.novicezk.midjourney.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@ApiModel("提交结果")
public class SubmitResultVO {

	@ApiModelProperty(value = "Status code: 1 (submission successful), 21 (existing), 22 (queuing), other (error)", required = true, example = "1")
	private int code;

	@ApiModelProperty(value = "describe", required = true, example = "Submitted successfully")
	private String description;

	@ApiModelProperty(value = "Task ID", example = "1320098173412546")
	private String result;

	@ApiModelProperty(value = "no")
	private Map<String, Object> properties = new HashMap<>();

	public SubmitResultVO setProperty(String name, Object value) {
		this.properties.put(name, value);
		return this;
	}

	public SubmitResultVO removeProperty(String name) {
		this.properties.remove(name);
		return this;
	}

	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getPropertyGeneric(String name) {
		return (T) getProperty(name);
	}

	public <T> T getProperty(String name, Class<T> clz) {
		return clz.cast(getProperty(name));
	}

	public static SubmitResultVO of(int code, String description, String result) {
		return new SubmitResultVO(code, description, result);
	}

	public static SubmitResultVO fail(int code, String description) {
		return new SubmitResultVO(code, description, null);
	}

	private SubmitResultVO(int code, String description, String result) {
		this.code = code;
		this.description = description;
		this.result = result;
	}
}
