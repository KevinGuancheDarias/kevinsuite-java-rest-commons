package com.kevinguanchedarias.kevinsuite.commons.rest.security.cors;

import com.kevinguanchedarias.kevinsuite.commons.rest.cors.exception.InvalidOriginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorsFilterTest {

	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	private SimpleCorsConfigurator corsConfiguratorSpy;
	private CorsFilter corsFilter;
	private MockFilterChain chainMock;
	private MockHttpServletRequest requestMock;
	private MockHttpServletResponse responseMock;

	@BeforeEach
	public void init() {
		chainMock = new MockFilterChain();
		requestMock = new MockHttpServletRequest();
		requestMock.addHeader("Access-Control-Request-Headers", "Fake header");
		responseMock = new MockHttpServletResponse();
		corsConfiguratorSpy = spy(new SimpleCorsConfigurator());
		corsConfiguratorSpy.setOriginList(new ArrayList<>());
		corsConfiguratorSpy.setRootOriginList(new ArrayList<>());
		reset(corsConfiguratorSpy);
		corsFilter = new CorsFilter();
		corsFilter.setCorsConfigurator(corsConfiguratorSpy);
	}

	@Test
	void should_do_nothing_when_origin_header_is_missing() {
		doFilter();

		verifyNoInteractions(corsConfiguratorSpy);
	}

	@Test
	void should_do_nothing_when_cors_configuration_is_null() {
		corsFilter.setCorsConfigurator(null);

		doFilter();

		verifyNoInteractions(corsConfiguratorSpy);
		assertFalse(responseMock.containsHeader("Access-Control-Allow-Origin"));
	}

	@Test
	void should_throw_no_valid_origin() {
		setOrigin("https://loltrain.com");

		assertThrows(InvalidOriginException.class, this::doFilter);
	}

	@Test
	void should_write_ok_when_http_method_is_options() throws UnsupportedEncodingException {
		requestMock.setMethod("OPTIONS");
		setValidOrigin();

		doFilter();

		assertEquals("OK", responseMock.getContentAsString());
	}

	@Test
	void should_properly_detect_root_domain() {
		setOrigin("https://universes-1.sgt.kevinguanchedarias.com:7070");
		corsConfiguratorSpy.getRootOriginList().add("kevinguanchedarias.com");

		doFilter();

		assertFalse(responseMock.containsHeader(ACCESS_CONTROL_ALLOW_METHODS));
	}

	@Test
	void should_write_methods_when_present() {
		setValidOrigin();
		List<String> methods = new ArrayList<>();
		methods.add("GET");
		methods.add("POST");
		corsConfiguratorSpy.setMethodList(methods);

		doFilter();

		assertTrue(responseMock.containsHeader(ACCESS_CONTROL_ALLOW_METHODS));
		assertEquals("GET, POST", responseMock.getHeaderValue(ACCESS_CONTROL_ALLOW_METHODS));
	}

	private void setValidOrigin() {
		String origin = "https://universes-1.sgt.kevinguanchedarias.com:7070";
		corsConfiguratorSpy.getOriginList().add(origin);
		setOrigin(origin);
	}

	private void setOrigin(String origin) {
		requestMock.addHeader("Origin", origin);
	}

	private void doFilter() {
		try {
			corsFilter.doFilter(requestMock, responseMock, chainMock);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}