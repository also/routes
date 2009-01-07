package com.ryanberdeen.routes;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

public class RouteUtils {
	public static final String MAPPING_ATTRIBUTE = HttpServletRequestMapping.class.getName();
	public static final String MATCH_ATTRIBUTE = RequestMatch.class.getName();

	public static HttpServletRequestMapping getMapping(ServletContext servletContext) {
		return (HttpServletRequestMapping) servletContext.getAttribute(MAPPING_ATTRIBUTE);
	}

	public static void setMapping(ServletContext servletContext, HttpServletRequestMapping mapping) {
		servletContext.setAttribute(MAPPING_ATTRIBUTE, mapping);
	}

	public static RequestMatch getMatch(ServletRequest request) {
		return (RequestMatch) request.getAttribute(MATCH_ATTRIBUTE);
	}

	public static void setMatch(ServletRequest request, RequestMatch match) {
		request.setAttribute(MATCH_ATTRIBUTE, match);
	}
}
