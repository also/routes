package com.ryanberdeen.routes;

import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

public class RouteUtils {
	@SuppressWarnings("unchecked")
	public static Mapping getMapping(ServletRequest request) {
		WebApplicationContext context = RequestContextUtils.getWebApplicationContext(request);
		Map<String, RouteHandlerMapping> mappings = context.getBeansOfType(RouteHandlerMapping.class);
		
		if (mappings.size() > 0) {
			return mappings.values().iterator().next();
		}
		else {
			return null;
		}
	}
	
	
	
}
