/**
 * 
 */
package com.kevinguanchedarias.kevinsuite.commons.rest.exception;

/**
 *
 * @since 0.2.0
 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
 */
public class MissingArgumentException extends CommonRestException {
	private static final long serialVersionUID = 5869113002108842843L;

	/**
	 * @param message
	 * @since 0.2.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public MissingArgumentException(String message) {
		super(message);
	}

}
