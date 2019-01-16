/**
 * 
 */
package com.kevinguanchedarias.kevinsuite.commons.rest.exception;

/**
 *
 * @since 0.2.0
 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
 */
public class FileNotFoundException extends CommonRestException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 * @since 0.2.0
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	public FileNotFoundException(String message) {
		super(message);
	}
}
