package com.ryanberdeen.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.ry1.springframework.web.util.ExtendedParameters;
import org.ry1.springframework.web.util.ExtendedParameters.Strategy;

public class RouteSet implements Mapping {
	/** The request attribute under which the matching Route is bound. */
	public static final String MATCHER_ATTRIBUTE_NAME = RouteSet.class.getName() + ".matcher";

	/** The request attribute under which the match result is bound. */
	public static final String MATCH_ATTRIBUTE_NAME = RouteSet.class.getName() + ".match";

	public static final String CONTEXT_PARAMETERS_ATTRIBUTE_NAME = RouteSet.class.getName() + ".contextParameters";

	private Strategy strategy = Strategy.WRAPPER;

	private List<Route> routes;

	private HashMap<String, Route> namedRoutes;

	private Set<String> contextParameterNames = Collections.singleton("controller");

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

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

	public UrlMatch getBestMatch(HttpServletRequest request, String url) throws Exception {
		Map<String, String> parameters = null;

		for (Route route: routes) {
			parameters = route.match(url, request);
			if (parameters != null) {
				UrlMatch match = new UrlMatch(route, parameters);
				return match;
			}
		}

		return null;
	}

	public void setCurrentMatch(HttpServletRequest request, UrlMatch match) {
		request.setAttribute(MATCHER_ATTRIBUTE_NAME, match.getRoute());
		request.setAttribute(MATCH_ATTRIBUTE_NAME, match.getParameters());

		HashMap<String, String> contextParameters = new HashMap<String, String>();

		for (Map.Entry<String, String> entry : match.getParameters().entrySet()) {
			if (contextParameterNames.contains(entry.getKey())) {
				contextParameters.put(entry.getKey(), entry.getValue());
			}
		}

		request.setAttribute(CONTEXT_PARAMETERS_ATTRIBUTE_NAME, contextParameters);

		ExtendedParameters.addExtendedParameter(request, strategy, match.getParameters());
	}

	public Route getNamedRoute(String name) {
		return namedRoutes.get(name);
	}

	public Route getBestMatch(Map<String, Object> parameters) {
		Map<String, String> contextParameters = Collections.emptyMap();
		return getBestMatch(parameters, contextParameters);
	}

	public Route getBestMatch(ServletRequest request, Map<String, Object> parameters) {
		return getBestMatch(parameters, getContextParameters(request));
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

	public String getUrl(HttpServletRequest request, String name, Map<String, Object> parameters, boolean includeContextPath) {
		Route route = getNamedRoute(name);

		return getUrl(request, parameters, route, includeContextPath);
	}

	public String getUrl(HttpServletRequest request, Map<String, Object> parameters, boolean includeContextPath) {
		Route route = getBestMatch(request, parameters);

		return getUrl(request, parameters, route,includeContextPath);
	}

	public String getUrl(HttpServletRequest request, Map<String, Object> parameters, Route route, boolean includeContextPath) {
		if (route == null) {
			return null;
		}

		String url = "" ;

		if (includeContextPath) {
			url = request.getContextPath();
		}

		url += route.buildUrl(parameters, getContextParameters(request));

		// FIXME query string

		return  url;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getContextParameters(ServletRequest request) {
		return (Map<String, String>) request.getAttribute(CONTEXT_PARAMETERS_ATTRIBUTE_NAME);
	}

	public Map<String, Route> getNamedRoutes() {
		return Collections.unmodifiableMap(namedRoutes);
	}

	public void prepare() throws Exception {
		// process route names
		namedRoutes = new HashMap<String, Route>();
		for (Route route : this.routes) {
			String name = route.getName();
			if (name != null) {
				namedRoutes.put(name, route);
			}
		}

		for (Route route : this.routes) {
			route.prepare();
		}
	}
}
