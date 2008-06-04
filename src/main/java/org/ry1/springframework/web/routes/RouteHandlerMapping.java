/* $Id$ */

package org.ry1.springframework.web.routes;

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
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

public class RouteHandlerMapping extends AbstractHandlerMapping implements Mapping {
	/** The request attribute under which the matching Route is bound. */
	public static final String MATCHER_ATTRIBUTE_NAME = RouteHandlerMapping.class.getName() + ".matcher";
	
	/** The rquest attribute under which the match result is bound. */
	public static final String MATCH_ATTRIBUTE_NAME = RouteHandlerMapping.class.getName() + ".match";
	
	public static final String CONTEXT_PARAMETERS_ATTRIBUTE_NAME = RouteHandlerMapping.class.getName() + ".contextParameters";

	private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
	
	private String controllerParameterName = "controller";
	private String controllerNameSuffix = "Controller";
	private Strategy strategy = Strategy.WRAPPER;
	
	private List<Route> routes;
	
	private HashMap<String, Route> namedRoutes;
	
	private Set<String> contextParameterNames = Collections.singleton("controller");
	
	private String javascriptRouteName = "routes.js";
	private String javascriptRouteUri = "/routes.js";
	private Route javascriptRoute;
	
	private HashMap<Route, Object> specialRoutes;
	
	public void setControllerParameterName(String controllerParameterName) {
		this.controllerParameterName = controllerParameterName;
	}
	
	public void setControllerNameSuffix(String controllerNameSuffix) {
		this.controllerNameSuffix = controllerNameSuffix;
	}
	
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	
	public void setRoutes(List<Route> routes) {
		this.routes = new ArrayList<Route>(routes.size() + 1);
		this.routes.addAll(routes);
		
		namedRoutes = new HashMap<String, Route>();
		javascriptRoute = new Route(javascriptRouteUri, EMPTY_PARAMETERS, javascriptRouteName);
		this.routes.add(javascriptRoute);
		
		specialRoutes = new HashMap<Route, Object>();
		specialRoutes.put(javascriptRoute, new RouteJavascriptController(this));
		
		// process route names
		for (Route route : this.routes) {
			String name = route.getName();
			if (name != null) {
				namedRoutes.put(name, route);
			}
		}
	}
	
	public void setContextParameterNames(Set<String> contextParameterNames) {
		this.contextParameterNames = contextParameterNames;
	}
	
	@Override
	protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
		Map<String, String> match = null;

		String url = URL_PATH_HELPER.getPathWithinApplication(request);
		
		for (Route route: routes) {
			match = route.match(url, request);
			if (match != null) {
				request.setAttribute(MATCHER_ATTRIBUTE_NAME, route);
				request.setAttribute(MATCH_ATTRIBUTE_NAME, match);
				
				HashMap<String, String> contextParameters = new HashMap<String, String>();
				
				for (Map.Entry<String, String> entry : match.entrySet()) {
					if (contextParameterNames.contains(entry.getKey())) {
						contextParameters.put(entry.getKey(), entry.getValue());
					}
				}
				
				request.setAttribute(CONTEXT_PARAMETERS_ATTRIBUTE_NAME, contextParameters);
				
				ExtendedParameters.addExtendedParameter(request, strategy, match);
				
				Object controller = specialRoutes.get(route);
				
				if (controller == null) {
					String controllerName = match.get(controllerParameterName);
					
					if (controllerName != null) {
						return controllerName + controllerNameSuffix;
					}
				}
				
				return controller;
			}
		}
		
		return null;
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
			throw new RuntimeException("No route matches " + parameters);
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
}
