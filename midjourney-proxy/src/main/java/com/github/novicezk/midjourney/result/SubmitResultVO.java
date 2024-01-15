package com.github.novicezk.midjourney.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@ApiModel("제출 결과")
public class SubmitResultVO {

	@ApiModelProperty(value = "상태 코드: 1(제출 성공), 21(이미 존재), 22(대기 중), 그 외(오류)", required = true, example = "1")
	private int code;

	@ApiModelProperty(value = "설명", required = true, example = "제출 성공")
	private String description;

	@ApiModelProperty(value = "Task ID", example = "1320098173412546")
	private String result;

	@ApiModelProperty(value = "확장 필드")
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
