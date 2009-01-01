package com.ryanberdeen.routes;

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public interface HttpServletRequestMapping extends Mapping {

	public Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

	/**
	 * @see Mapping#getBestMatch(String, String)
	 */
	public UrlMatch getBestMatch(HttpServletRequest request, String path);

	/**
	 * @see Mapping#getBestMatch(Map, Map)
	 */
	public Route getBestMatch(ServletRequest request, Map<String, Object> parameters);

	/**
	 * @see Mapping#getPath(String, Map, Map)
	 */
	public String getPath(HttpServletRequest request, String name, Map<String, Object> parameters, boolean includeContextPath);

	/**
	 * @see Mapping#getPath(Map, Map)
	 */
	public String getPath(HttpServletRequest request, Map<String, Object> parameters, boolean includeContextPath);
}