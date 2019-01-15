package com.kevinguanchedarias.kevinsuite.commons.rest.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * This class is invoked whn the authentication has failed<br />
 * Overrides the default behavior of Spring (redirect if failed)<br />
 * Insteat it will send a 401 Unauthorized
 * 
 * @author Kevin Guanche Darias
 *
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

}
