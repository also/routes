/* $Id$ */

package org.ry1.springframework.web.routes;

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public interface Mapping {

	public Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

	public abstract DefaultRoute getNamedRoute(String name);

	public abstract DefaultRoute getBestMatch(Map<String, Object> parameters);

	public abstract DefaultRoute getBestMatch(ServletRequest request,
			Map<String, Object> parameters);

	public abstract DefaultRoute getBestMatch(Map<String, Object> parameters,
			Map<String, String> contextParameters);

	public abstract String getUrl(HttpServletRequest request, String name,
			Map<String, Object> parameters, boolean includeContextPath);

	public abstract String getUrl(HttpServletRequest request,
			Map<String, Object> parameters, boolean includeContextPath);

	public abstract String getUrl(HttpServletRequest request,
			Map<String, Object> parameters, DefaultRoute route,
			boolean includeContextPath);
	
	public Map<String, DefaultRoute> getNamedRoutes();

}