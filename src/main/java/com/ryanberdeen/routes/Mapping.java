/* $Id$ */

package com.ryanberdeen.routes;

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public interface Mapping {

	public Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

	public abstract Route getNamedRoute(String name);

	public abstract Route getBestMatch(Map<String, Object> parameters);

	public abstract Route getBestMatch(ServletRequest request,
			Map<String, Object> parameters);

	public abstract Route getBestMatch(Map<String, Object> parameters,
			Map<String, String> contextParameters);

	public abstract String getUrl(HttpServletRequest request, String name,
			Map<String, Object> parameters, boolean includeContextPath);

	public abstract String getUrl(HttpServletRequest request,
			Map<String, Object> parameters, boolean includeContextPath);

	public abstract String getUrl(HttpServletRequest request,
			Map<String, Object> parameters, Route route,
			boolean includeContextPath);
	
	public Map<String, Route> getNamedRoutes();

}