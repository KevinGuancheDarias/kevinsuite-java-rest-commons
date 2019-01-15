package com.kevinguanchedarias.kevinsuite.commons.rest.security;

public interface FilterEventHandler {

	/**
	 * Will be run before authenticating<br />
	 * Even before checking the token is valid
	 * 
	 * @author Kevin Guanche Darias
	 */
	public void doBefore();

	/**
	 * Will be run after authenticating. Has access to the logged in user token
	 * data
	 * 
	 * @author Kevin Guanche Darias
	 */
	public void doAfter();
}
