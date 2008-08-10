package com.ryanberdeen.routes.xml;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.ryanberdeen.routes.Route;

public class AppliedRouteFactoryBean implements FactoryBean, InitializingBean {
	private Route route;
	private Map<String, String> parameters;
	private Set<String> methods;
	private Set<String> excludedMethods;
	private Route object;
	
	public void setRoute(Route route) {
		this.route = route;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	public void setMethods(Set<String> methods) {
		this.methods = methods;
	}
	
	public void setExcludedMethods(Set<String> excludedMethods) {
		this.excludedMethods = excludedMethods;
	}
	
	public Route getObject() throws Exception {
		return object;
	}

	public Class<Route> getObjectType() {
		return Route.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		object = route.apply(parameters, methods, excludedMethods);
	}

}
