package com.ryanberdeen.routes;

import javax.servlet.ServletContext;

public class RouteUtils {
	public static Mapping getMapping(ServletContext servletContext) {
		return (Mapping) servletContext.getAttribute(Mapping.class.getName());
	}
	
	public static void setMapping(ServletContext servletContext, Mapping mapping) {
		servletContext.setAttribute(Mapping.class.getName(), mapping);
	}
}
