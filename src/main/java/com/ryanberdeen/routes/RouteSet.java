package com.ryanberdeen.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class RouteSet implements HttpServletRequestMapping {
	private List<Route> routes;

	private HashMap<String, Route> namedRoutes;

	private Set<String> contextParameterNames = Collections.singleton("controller");

	public void setRoutes(List<Route> routes) {
		this.routes = new ArrayList<Route>(routes.size() + 1);
		this.routes.addAll(routes);
	}

	public void addRoute(Route route) {
		routes.add(route);
	}

	public void setContextParameterNames(Set<String> contextParameterNames) {
		this.contextParameterNames = contextParameterNames;
	}

	public RequestMatch getBestMatch(String method, String path) {
		Map<String, String> parameters = null;

		for (Route route: routes) {
			parameters = route.match(path, method);
			if (parameters != null) {
				HashMap<String, String> contextParameters = new HashMap<String, String>();

				for (Map.Entry<String, String> entry : parameters.entrySet()) {
					if (contextParameterNames.contains(entry.getKey())) {
						contextParameters.put(entry.getKey(), entry.getValue());
					}
				}

				RequestMatch match = new RequestMatch(route, parameters, contextParameters);
				return match;
			}
		}

		return null;
	}

	public RequestMatch getBestMatch(HttpServletRequest request, String path) {
		return getBestMatch(request.getMethod(), path);
	}

	public Route getNamedRoute(String name) {
		return namedRoutes.get(name);
	}

	public Route getBestMatch(Map<String, Object> parameters) {
		Map<String, String> contextParameters = Collections.emptyMap();
		return getBestMatch(parameters, contextParameters);
	}

	public Route getBestMatch(RequestMatch match, Map<String, Object> parameters) {
		return getBestMatch(parameters, match.getContextParameters());
	}

	public Route getBestMatch(Map<String, Object> parameters, Map<String, String> contextParameters) {
		int bestMatchCount = 0;
		Route bestMatch = null;
		for (Route route : routes) {
			int matchCount = route.match(parameters, contextParameters);
			if (matchCount > bestMatchCount) {
				bestMatch = route;
				bestMatchCount = matchCount;
			}
		}

		return bestMatch;
	}

	public String getPath(RequestMatch match, String name, Map<String, Object> parameters) {
		return getPath(name, parameters, match.getContextParameters());
	}

	public String getPath(Map<String, Object> parameters, Map<String, String> contextParameters) {
		Route route = getBestMatch(parameters, contextParameters);
		if (route == null) {
			throw new PathGenerationException("No route matches parameters " + parameters + ", contextParameters " + contextParameters);
		}
		return buildPath(route, parameters, contextParameters);
	}

	public String getPath(RequestMatch match, Map<String, Object> parameters) {
		return getPath(parameters, match.getContextParameters());
	}

	public String getPath(String name, Map<String, Object> parameters, Map<String, String> contextParameters) {
		Route route = getNamedRoute(name);
		if (route == null) {
			throw new PathGenerationException("No route matches name " + name);
		}

		return buildPath(route, parameters, contextParameters);
	}

	private static String buildPath(Route route, Map<String, Object> parameters, Map<String, String> contextParameters) {
		// FIXME query string
		return route.buildUrl(parameters, contextParameters);
	}

	public Map<String, Route> getNamedRoutes() {
		return Collections.unmodifiableMap(namedRoutes);
	}

	public void prepare() throws Exception {
		// process route names
		namedRoutes = new HashMap<String, Route>();
		for (Route route : routes) {
			String name = route.getName();
			if (name != null) {
				namedRoutes.put(name, route);
			}
		}

		for (Route route : routes) {
			route.prepare();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		for (Route route : routes) {
			builder.append('\n');
			builder.append(" * " + route.getUrlPattern().getRegex());
		}

		return builder.toString();
	}
}
