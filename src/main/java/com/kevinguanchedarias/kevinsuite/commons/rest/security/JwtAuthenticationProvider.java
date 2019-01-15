package com.kevinguanchedarias.kevinsuite.commons.rest.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

public class JwtAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) {
		TokenUser user = (TokenUser) authentication.getCredentials();
		user.setAuthenticated(true);
		return user;
	}

	@Override
	public boolean supports(Class<?> authenticate) {
		return authenticate.equals(TokenUser.class);
	}

}
