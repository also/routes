/* $Id$ */

package org.ry1.springframework.web.routes.xml;

import java.util.Map;

class RouteParameters {
	public Map<String, String> metaParameters;
	public Map<String, String> routeParameters;
	
	public RouteParameters(Map<String, String> metaParameters, Map<String, String> routeParameters) {
		this.metaParameters = metaParameters;
		this.routeParameters = routeParameters;
	}
	
	public String getMetaParameter(String name, String defaultValue) {
		String result = metaParameters.get(name);
		return result != null ? result : defaultValue;
	}
}
