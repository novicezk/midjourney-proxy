package com.github.novicezk.midjourney.exception;

public class ConnectionManuallyClosedException extends Exception {
	public ConnectionManuallyClosedException(String message) {
		super(message);
	}
}