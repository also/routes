package com.ryanberdeen.routes;

import java.util.Map;

public class UrlMatch {
	private Route route;
	private Map<String, String> parameters;

	UrlMatch(Route route, Map<String, String> parameters) {
		this.route = route;
		this.parameters = parameters;
	}

	public Route getRoute() {
		return route;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
}
