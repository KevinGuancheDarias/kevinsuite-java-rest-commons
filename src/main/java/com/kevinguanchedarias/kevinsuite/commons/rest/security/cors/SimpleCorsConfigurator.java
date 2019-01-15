package com.kevinguanchedarias.kevinsuite.commons.rest.security.cors;

import java.util.ArrayList;
import java.util.List;

public class SimpleCorsConfigurator implements CorsConfigurator {

	private List<String> originList;
	private List<String> rootOriginList;
	private List<String> methodList;
	private List<String> headerList;

	@Override
	public List<String> getOriginList() {
		return originList;
	}

	public void setOriginList(List<String> originList) {
		this.originList = originList;
	}

	/**
	 * Root domains, that should be accepted For example: specify
	 * <b>kevinguanchedarias.com</b> to accept all origins originating from
	 * kevinguanchedarias.com
	 * 
	 * @return
	 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
	 */
	@Override
	public List<String> getRootOriginList() {
		return rootOriginList;
	}

	public void setRootOriginList(List<String> rootOriginList) {
		this.rootOriginList = rootOriginList;
	}

	@Override
	public List<String> getMethodList() {
		return methodList;
	}

	public void setMethodList(List<String> methodList) {
		this.methodList = methodList;
	}

	@Override
	public List<String> getHeaderList() {
		return headerList;
	}

	public void setHeaderList(List<String> headerList) {
		this.headerList = headerList;
	}

	@Override
	public void addOrigin(String origin) {
		addAndCreateIfRequired(origin, originList);

	}

	@Override
	public void addMethod(String method) {
		addAndCreateIfRequired(method, methodList);

	}

	@Override
	public void addHeader(String header) {
		addAndCreateIfRequired(header, headerList);

	}

	private List<String> addAndCreateIfRequired(String value, List<String> source) {
		List<String> retVal = source;
		if (source == null) {
			retVal = new ArrayList<>();
		}

		retVal.add(value);
		return retVal;
	}
}
