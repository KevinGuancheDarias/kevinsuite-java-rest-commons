package com.kevinguanchedarias.kevinsuite.commons.rest.cors.exception;

import com.kevinguanchedarias.kevinsuite.commons.rest.exception.CommonRestException;

public class InvalidOriginException extends CommonRestException {
	private static final long serialVersionUID = -9113640521554435636L;

	public InvalidOriginException(String message) {
		super(message);
	}

}
