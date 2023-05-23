package com.github.novicezk.midjourney.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel("返回结果")
public class Message<T> {
	@ApiModelProperty("状态码: 1成功, 2提示, 其他错误")
	private final int code;
	@ApiModelProperty("描述")
	private final String description;
	private final T result;

	public static final int SUCCESS_CODE = 1;

	public static final int WAITING_CODE = 2;
	public static final int NOT_FOUND_CODE = 3;
	public static final int VALIDATION_ERROR_CODE = 4;
	public static final int FAILURE_CODE = 9;

	public static <Y> Message<Y> success() {
		return new Message<>(SUCCESS_CODE, "成功");
	}

	public static <T> Message<T> success(T result) {
		return new Message<>(SUCCESS_CODE, "成功", result);
	}

	public static <T> Message<T> success(int code, String description, T result) {
		return new Message<>(code, description, result);
	}

	public static <Y> Message<Y> notFound() {
		return new Message<>(NOT_FOUND_CODE, "数据未找到");
	}

	public static <Y> Message<Y> validationError() {
		return new Message<>(VALIDATION_ERROR_CODE, "校验错误");
	}

	public static <Y> Message<Y> failure() {
		return new Message<>(FAILURE_CODE, "系统异常");
	}

	public static <Y> Message<Y> failure(String description) {
		return new Message<>(FAILURE_CODE, description);
	}

	public static <Y> Message<Y> of(int code, String description) {
		return new Message<>(code, description);
	}

	public static <T> Message<T> of(int code, String description, T result) {
		return new Message<>(code, description, result);
	}

	private Message(int code, String description) {
		this(code, description, null);
	}

	private Message(int code, String description, T result) {
		this.code = code;
		this.description = description;
		this.result = result;
	}
}
