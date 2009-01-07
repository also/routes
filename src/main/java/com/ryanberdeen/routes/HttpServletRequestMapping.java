package com.ryanberdeen.routes;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface HttpServletRequestMapping extends Mapping {

	public Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

	/**
	 * @see Mapping#getBestMatch(String, String)
	 */
	public RequestMatch getBestMatch(HttpServletRequest request, String path);

	/**
	 * @see Mapping#getBestMatch(Map, Map)
	 */
	public Route getBestMatch(RequestMatch match, Map<String, Object> parameters);

	/**
	 * @see Mapping#getPath(String, Map, Map)
	 */
	public String getPath(RequestMatch match, String name, Map<String, Object> parameters);

	/**
	 * @see Mapping#getPath(Map, Map)
	 */
	public String getPath(RequestMatch match, Map<String, Object> parameters);
}