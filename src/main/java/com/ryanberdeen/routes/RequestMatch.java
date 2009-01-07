package com.ryanberdeen.routes;

import java.util.Map;

public class RequestMatch {
	private Route route;
	private Map<String, String> parameters;
	private Map<String, String> contextParameters;

	RequestMatch(Route route, Map<String, String> parameters, Map<String, String> contextParameters) {
		this.route = route;
		this.parameters = parameters;
		this.contextParameters = contextParameters;
	}

	public Route getRoute() {
		return route;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public Map<String, String> getContextParameters() {
		return contextParameters;
	}
}
