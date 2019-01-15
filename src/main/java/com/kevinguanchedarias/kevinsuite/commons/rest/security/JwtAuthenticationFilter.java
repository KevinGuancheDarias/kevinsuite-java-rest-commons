package com.kevinguanchedarias.kevinsuite.commons.rest.security;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.CommonJwtException;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.CommonRestException;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.InvalidAuthorizationHeader;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.JwtTokenExpired;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo.BackendErrorPojo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	private static final Logger LOCAL_LOGGER = Logger.getLogger(JwtAuthenticationFilter.class);

	private TokenConfigLoader tokenConfigLoader;
	private FilterEventHandler filterEventHandler;

	private Boolean convertExceptionToJson = false;

	public JwtAuthenticationFilter() {
		super("/**");
	}

	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		return true;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			if (filterEventHandler != null) {
				filterEventHandler.doBefore();
			}

			TokenUser user = decodeTokenIfPossible(findTokenInRequest(request));
			return getAuthenticationManager().authenticate((Authentication) user);
		} catch (CommonJwtException | CommonRestException e) {
			LOCAL_LOGGER.info(e.getMessage());
			sendJsonOrThrowException(response, e);
		} catch (RuntimeException e) {
			LOCAL_LOGGER.error("Fatal error occured", e);
			sendJsonOrThrowException(response, e);
		}
		return null;
	}

	public TokenConfigLoader getTokenConfigLoader() {
		return tokenConfigLoader;
	}

	public void setTokenConfigLoader(TokenConfigLoader tokenConfigLoader) {
		this.tokenConfigLoader = tokenConfigLoader;
	}

	public FilterEventHandler getFilterEventHandler() {
		return filterEventHandler;
	}

	/**
	 * Listen to token validation messages, and customize application behavior
	 * <br>
	 * <b>Set to something implementing the FilterEventHandler interface</b>
	 * 
	 * @param filterEventHandler
	 * @author Kevin Guanche Darias
	 */
	public void setFilterEventHandler(FilterEventHandler filterEventHandler) {
		this.filterEventHandler = filterEventHandler;
	}

	public Boolean getConvertExceptionToJson() {
		return convertExceptionToJson;
	}

	/**
	 * Set to true, so, instead of throwing exception to the application server,
	 * it will response a JSON 500 error
	 * 
	 * @param convertExceptionToJson
	 * @author Kevin Guanche Darias
	 */
	public void setConvertExceptionToJson(Boolean convertExceptionToJson) {
		this.convertExceptionToJson = convertExceptionToJson;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);

		if (filterEventHandler != null) {
			filterEventHandler.doAfter();
		}
		// Ensure execution follows after validating token, else, request won't
		// never touch the rest controllers!
		chain.doFilter(request, response);
	}

	@SuppressWarnings("unchecked")
	protected TokenUser decodeTokenIfPossible(String token) {
		TokenUser user = null;
		try {
			Claims body = getTokenClaimsIfNotExpired(token);
			HashMap<String, Object> userData = (HashMap<String, Object>) body.get("data");
			user = new TokenUser();
			user.setId((Number) userData.get("id"));
			user.setUsername((String) userData.get("username"));
			user.setEmail((String) userData.get("email"));
		} catch (MalformedJwtException e) {
			throw new InvalidAuthorizationHeader(e.getMessage(), e);
		}

		return user;
	}

	protected Claims getTokenClaimsIfNotExpired(String token) {
		Claims body = Jwts.parser().setSigningKey(tokenConfigLoader.getTokenSecret()).parseClaimsJws(token).getBody();
		Date now = new Date();
		Date expiration = new Date(body.getExpiration().getTime() / 1000);
		if (now.after(expiration)) {
			throw new JwtTokenExpired("Session has expired");
		}
		return body;
	}

	/**
	 * Will return the JWT token obtained from HTTP Authorization header
	 * 
	 * @return
	 * @author Kevin Guanche Darias
	 */
	protected String findTokenInRequest(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		final int tokenBegin = 7;
		checkValidAuthorizationHeader(header);
		return header.substring(tokenBegin);
	}

	protected void checkValidAuthorizationHeader(String headerContent) {
		if (headerContent == null || !headerContent.startsWith("Bearer ")) {
			throw new InvalidAuthorizationHeader("HTTP Authorization header not found, or it's invalid");
		}
	}

	/**
	 * Instead of throwing exception, creates a JSON encoded error Or throws
	 * exception if convertExceptionToJson is false
	 * 
	 * @param response
	 * @param e
	 *            Exception information
	 * @author Kevin Guanche Darias
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	protected void sendJsonOrThrowException(HttpServletResponse response, RuntimeException e) throws IOException {
		if (convertExceptionToJson) {
			ObjectMapper mapper = new ObjectMapper();
			BackendErrorPojo errorPojo = new BackendErrorPojo();
			errorPojo.setExceptionType(e.getClass().getSimpleName());
			errorPojo.setMessage(e.getMessage());
			response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().print(mapper.writeValueAsString(errorPojo));
		} else {
			throw e;
		}
	}
}