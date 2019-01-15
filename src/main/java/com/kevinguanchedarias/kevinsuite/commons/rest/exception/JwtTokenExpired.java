package com.kevinguanchedarias.kevinsuite.commons.rest.exception;

public class JwtTokenExpired extends CommonJwtException {
	private static final long serialVersionUID = 1924762059045904L;

	public JwtTokenExpired(String message) {
		super(message);
	}

}