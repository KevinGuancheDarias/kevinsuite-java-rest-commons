package com.kevinguanchedarias.kevinsuite.commons.rest.exception;

public class CommonRestException extends RuntimeException {
	private static final long serialVersionUID = 3501404123991303606L;

	public CommonRestException(String message) {
		super(message);
	}

	public CommonRestException(String message, Exception e) {
		super(message, e);
	}
}
