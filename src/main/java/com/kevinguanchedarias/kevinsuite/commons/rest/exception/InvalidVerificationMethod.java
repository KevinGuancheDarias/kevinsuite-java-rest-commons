/**
 * 
 */
package com.kevinguanchedarias.kevinsuite.commons.rest.exception;

/**
 *
 * @since 0.2.0
 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
 */
public class InvalidVerificationMethod extends CommonRestException {
	private static final long serialVersionUID = 467124034650247588L;

	/**
	 * @param message
	 * @since 0.2.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public InvalidVerificationMethod(String message) {
		super(message);
	}

}
