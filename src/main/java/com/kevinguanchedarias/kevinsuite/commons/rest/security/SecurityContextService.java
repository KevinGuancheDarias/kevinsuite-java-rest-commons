package com.kevinguanchedarias.kevinsuite.commons.rest.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This is a spring bean that is not automatically loaded<br />
 * MUST define it in security-context in order to use it<br />
 * <b>Overrides static calls to {@link SecurityContextHolder} </b> <b>So it's
 * easy to mock it!</b>
 * 
 * @author Kevin Guanche Darias
 *
 */
public class SecurityContextService {

	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
}
