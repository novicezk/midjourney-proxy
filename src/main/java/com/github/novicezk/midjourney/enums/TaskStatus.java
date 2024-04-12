package com.github.novicezk.midjourney.enums;


import lombok.Getter;

public enum TaskStatus {
	/**
	 * have not started.
	 */
	NOT_START(0),
	/**
	 * submitted.
	 */
	SUBMITTED(1),
	/**
	 * Executing.
	 */
	IN_PROGRESS(3),
	/**
	 * fail.
	 */
	FAILURE(4),
	/**
	 * success.
	 */
	SUCCESS(4);

	@Getter
	private final int order;

	TaskStatus(int order) {
		this.order = order;
	}

}
