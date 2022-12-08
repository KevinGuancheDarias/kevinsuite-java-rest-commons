package com.kevinguanchedarias.kevinsuite.commons.rest.security.cors;

import com.kevinguanchedarias.kevinsuite.commons.rest.cors.exception.InvalidOriginException;
import com.kevinguanchedarias.kevinsuite.commons.rest.exception.CommonJwtException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
public class CorsFilter extends OncePerRequestFilter {

	private static final String ALLOW_ANY_ORIGIN = "*";
	private static final String MAX_CACHE_SIZE = "86400";

	@Getter
	private CorsConfigurator corsConfigurator;

	public void setCorsConfigurator(CorsConfigurator corsConfigurator) {
		this.corsConfigurator = corsConfigurator;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String clientOriginHeader = request.getHeader("Origin");
		if (clientOriginHeader != null && corsConfigurator != null) {
			checkValidOrigin(clientOriginHeader);
			addHeadersFromConfigurator(request, response);
		} else if (corsConfigurator == null) {
			log.warn(this.getClass().getName() + " is doing nothing, as CorsConfigurator has not been set!");
		} else {
			log.debug("Client didn't send the origin header");
		}

		if ("OPTIONS".equals(request.getMethod())) {
			response.getWriter().print("OK");
			response.getWriter().flush();

		} else {
			chain.doFilter(request, response);
		}

	}

	private void addHeadersFromConfigurator(HttpServletRequest request, HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Max-Age", MAX_CACHE_SIZE);
		if (corsConfigurator.getHeaderList() != null) {
			throw new AssertionError(
					"Use headers from corsConfigurator has not been implemented, is it even required?");
		}
		if (corsConfigurator.getMethodList() != null) {
			response.setHeader("Access-Control-Allow-Methods", String.join(", ", corsConfigurator.getMethodList()));
		}

		response.addHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
	}

	private void checkValidOrigin(String clientOriginHeader) {
		List<String> originList = corsConfigurator.getOriginList();
		if (!originList.contains(ALLOW_ANY_ORIGIN) && !originList.contains(clientOriginHeader)
				&& !isFromRootDomain(clientOriginHeader)) {
			throw new InvalidOriginException("Origin " + clientOriginHeader + " is not authorized to use the service");
		}
	}

	private boolean isFromRootDomain(String clientOriginHeader) {
		String domain;
		try {
			domain = new URI(clientOriginHeader).getHost();
		} catch (URISyntaxException e) {
			throw new CommonJwtException("Bad HTTP Origin header", e);
		}
		return corsConfigurator.getRootOriginList().stream().anyMatch(domain::endsWith);
	}
}
