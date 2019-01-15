package com.kevinguanchedarias.kevinsuite.commons.rest.security;

/**
 * In order to define a custom loader must extend this, and AuthenticationFilter
 * will use one object of this interface to find out JWT token secret
 * 
 * @author Kevin Guanche Darias
 *
 */
@FunctionalInterface
public interface TokenConfigLoader {
	public String getTokenSecret();
}
