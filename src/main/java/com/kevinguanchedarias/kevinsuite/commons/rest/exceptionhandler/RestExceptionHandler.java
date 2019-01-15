package com.kevinguanchedarias.kevinsuite.commons.rest.exceptionhandler;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo.BackendErrorPojo;

/**
 * Inherit from this class to handle exceptions put the
 * annotation @ControllerAdvice in the children class
 * 
 * @author Kevin Guanche Darias
 *
 */
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger LOCAL_LOGGER = Logger.getLogger(RestExceptionHandler.class);

	@ExceptionHandler({ Exception.class })
	protected ResponseEntity<Object> handleAnyException(Exception e, WebRequest request) {
		BackendErrorPojo response = new BackendErrorPojo();
		response.setExceptionType("InternalServerError");
		response.setMessage("Unexpected server error");
		LOCAL_LOGGER.error(e.getMessage(), e);

		return handleExceptionInternal(e, response, prepareCommonHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException e,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return handleGameException(e, request);
	}

	/**
	 * Handles game exceptions, just call it from custom @ExceptionHandler
	 * <br />
	 * 
	 * @param e
	 * @param request
	 * @return
	 * @author Kevin Guanche Darias
	 */
	protected ResponseEntity<Object> handleGameException(Exception e, WebRequest request) {
		return handleGameException(e, request, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handles game exceptions, just call it from custom @ExceptionHandler
	 * <br />
	 * 
	 * @param e
	 * @param request
	 * @return
	 * @author Kevin Guanche Darias
	 */
	protected ResponseEntity<Object> handleGameException(Exception e, WebRequest request, HttpStatus status) {
		BackendErrorPojo response = new BackendErrorPojo();
		response.setExceptionType(e.getClass().getSimpleName());
		response.setMessage(e.getMessage());
		LOCAL_LOGGER.debug(e);
		return handleExceptionInternal(e, response, prepareCommonHeaders(), status, request);
	}

	protected HttpHeaders prepareCommonHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return headers;
	}
}
