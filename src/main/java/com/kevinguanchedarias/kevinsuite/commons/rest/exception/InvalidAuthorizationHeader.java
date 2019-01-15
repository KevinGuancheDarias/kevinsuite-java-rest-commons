package com.kevinguanchedarias.kevinsuite.commons.rest.exception;

public class InvalidAuthorizationHeader extends CommonRestException {
	private static final long serialVersionUID = -4378043543004097967L;

	public InvalidAuthorizationHeader(String message) {
		super(message);
	}

	public InvalidAuthorizationHeader(String message, Exception e) {
		super(message, e);
	}
}
