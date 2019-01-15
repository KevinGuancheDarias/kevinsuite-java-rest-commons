package com.kevinguanchedarias.kevinsuite.commons.rest.exception;

public class CommonJwtException extends RuntimeException {
	private static final long serialVersionUID = 377118478735314404L;

	public CommonJwtException(String message) {
		super(message);
	}

	public CommonJwtException(String message, Exception e) {
		super(message, e);
	}

}
