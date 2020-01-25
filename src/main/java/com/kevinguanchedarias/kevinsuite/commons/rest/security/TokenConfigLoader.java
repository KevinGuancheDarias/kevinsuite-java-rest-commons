package com.kevinguanchedarias.kevinsuite.commons.rest.security;

import com.kevinguanchedarias.kevinsuite.commons.rest.security.enumerations.TokenVerificationMethod;

/**
 * In order to define a custom loader must extend this, and AuthenticationFilter
 * will use one object of this interface to find out JWT token secret
 * 
 * @since 0.1.0
 * @author Kevin Guanche Darias
 *
 */
public interface TokenConfigLoader {
	/**
	 * 
	 * @return The secret used to validate the tokens
	 * @since 0.1.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public String getTokenSecret();

	/**
	 * Defines the verification method used
	 * 
	 * @return
	 * @since 0.2.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public TokenVerificationMethod getVerificationMethod();

	/**
	 * Gets the path to the private key file
	 * 
	 * @return
	 * @since 0.2.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public String getPrivateKey();

	/**
	 * Gets the path to the public key file
	 * 
	 * @return
	 * @since 0.2.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public String getPublicKey();

	/**
	 * Defines the allowed clock skew in secons
	 * 
	 * @return
	 * @since 0.4.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public default long getAllowedClockSkew() {
		return 0L;
	}
}
