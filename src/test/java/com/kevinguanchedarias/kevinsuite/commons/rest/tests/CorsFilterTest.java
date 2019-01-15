package com.kevinguanchedarias.kevinsuite.commons.rest.tests;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.kevinguanchedarias.kevinsuite.commons.rest.cors.exception.InvalidOriginException;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.cors.CorsFilter;
import com.kevinguanchedarias.kevinsuite.commons.rest.security.cors.SimpleCorsConfigurator;

public class CorsFilterTest {

	private SimpleCorsConfigurator corsConfigurator;
	private CorsFilter corsFilter;
	private MockFilterChain chainMock;
	private MockHttpServletRequest requestMock;
	private MockHttpServletResponse responseMock;

	@Before
	public void init() {
		chainMock = new MockFilterChain();
		requestMock = new MockHttpServletRequest();
		requestMock.addHeader("Access-Control-Request-Headers", "Fake header");
		responseMock = new MockHttpServletResponse();
		corsConfigurator = new SimpleCorsConfigurator();
		corsConfigurator.setOriginList(new ArrayList<>());
		corsConfigurator.setRootOriginList(new ArrayList<>());
		corsFilter = new CorsFilter();
		corsFilter.setCorsConfigurator(corsConfigurator);
	}

	@Test
	public void shouldDoNothingWhenOriginHeaderIsMissing() {
		doFilter();
	}

	@Test(expected = InvalidOriginException.class)
	public void shouldThroNoValidOrigin() {
		setOrigin("lol");
		doFilter();
	}

	@Test
	public void shouldProperlyDetectRootDomain() {
		setOrigin("http://universes-1.sgt.kevinguanchedarias.com:7070");
		corsConfigurator.getRootOriginList().add("kevinguanchedarias.com");
		doFilter();
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