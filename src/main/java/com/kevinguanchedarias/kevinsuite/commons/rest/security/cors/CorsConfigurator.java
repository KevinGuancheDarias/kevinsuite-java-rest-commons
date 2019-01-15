package com.kevinguanchedarias.kevinsuite.commons.rest.security.cors;

import java.util.List;

public interface CorsConfigurator {
	public List<String> getOriginList();

	public List<String> getRootOriginList();

	public List<String> getMethodList();

	public List<String> getHeaderList();

	public void addOrigin(String origin);

	public void addMethod(String method);

	public void addHeader(String header);
}
