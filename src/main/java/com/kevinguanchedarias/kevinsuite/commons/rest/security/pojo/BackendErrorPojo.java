package com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo;

public class BackendErrorPojo {
	private String message;
	private String exceptionType;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

}
