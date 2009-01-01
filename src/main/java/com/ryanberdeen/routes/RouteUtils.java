package com.ryanberdeen.routes;

import javax.servlet.ServletContext;

public class RouteUtils {
	public static HttpServletRequestMapping getMapping(ServletContext servletContext) {
		return (HttpServletRequestMapping) servletContext.getAttribute(HttpServletRequestMapping.class.getName());
	}
	
	public static void setMapping(ServletContext servletContext, HttpServletRequestMapping mapping) {
		servletContext.setAttribute(HttpServletRequestMapping.class.getName(), mapping);
	}
}
