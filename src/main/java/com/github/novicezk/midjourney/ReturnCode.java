package com.github.novicezk.midjourney;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReturnCode {
	/**
	 * success.
	 */
	public static final int SUCCESS = 1;
	/**
	 * Data not found.
	 */
	public static final int NOT_FOUND = 3;
	/**
	 * Verification error.
	 */
	public static final int VALIDATION_ERROR = 4;
	/**
	 * System exception.
	 */
	public static final int FAILURE = 9;

	/**
	 * existed.
	 */
	public static final int EXISTED = 21;
	/**
	 * in the line.
	 */
	public static final int IN_QUEUE = 22;
	/**
	 * The queue is full.
	 */
	public static final int QUEUE_REJECTED = 23;
	/**
	 * prompt contains sensitive words.
	 */
	public static final int BANNED_PROMPT = 24;

}