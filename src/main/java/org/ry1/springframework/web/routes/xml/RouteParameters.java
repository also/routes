/* $Id$ */

package org.ry1.springframework.web.routes.xml;

import java.util.HashMap;

class RouteParameters implements Cloneable {
	public HashMap<String, String> metaParameters;
	public HashMap<String, String> routeParameters;
	public HashMap<String, String> routeRegexes;
	
	public RouteParameters() {
		initDefault();
	}
	
	public RouteParameters(RouteParameters that) {
		if (that != null) {
			metaParameters = new HashMap<String, String>(that.metaParameters);
			routeParameters = new HashMap<String, String>(that.routeParameters);
		}
		else {
			initDefault();
		}
	}
	
	private void initDefault() {
		metaParameters = new HashMap<String, String>();
		routeParameters = new HashMap<String, String>();
	}
	
	public String getMetaParameter(String name) {
		return metaParameters.get(name);
	}
	
	public String getMetaParameter(String name, String defaultValue) {
		String result = metaParameters.get(name);
		return result != null ? result : defaultValue;
	}
	
	@Override
	protected RouteParameters clone() throws CloneNotSupportedException {
		return new RouteParameters(this);
	}
}
