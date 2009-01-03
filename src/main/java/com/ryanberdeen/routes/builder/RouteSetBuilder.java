package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ryanberdeen.routes.Route;
import com.ryanberdeen.routes.RouteSet;
import com.ryanberdeen.routes.UrlPattern;

public class RouteSetBuilder implements RouteListBuilder, RouteOptions {
	private HashMap<String, RouteSetBuilderTemplate> templates = new HashMap<String, RouteSetBuilderTemplate>();
	private ArrayList<RouteListBuilder> routeListBuilders = new ArrayList<RouteListBuilder>();

	private RouteBuilder routeDefinition;

	public RouteSetBuilder() {
		routeDefinition = new RouteBuilder();
	}

	public RouteSetBuilder(RouteSetBuilder that) {
		templates = new HashMap<String, RouteSetBuilderTemplate>(that.templates);
		routeDefinition = new RouteBuilder(that.routeDefinition);
	}

	public RouteSetBuilder nested() {
		RouteSetBuilder nested = new RouteSetBuilder(this);
		routeListBuilders.add(nested);
		return nested;
	}

	public RouteBuilder match() {
		RouteBuilder routeDefinition = this.routeDefinition.clone();
		routeListBuilders.add(new SingleRouteDefinition(routeDefinition));
		return routeDefinition;
	}

	public RouteBuilder match(String pattern) {
		RouteBuilder routeDefinition = match();
		routeDefinition.setPattern(pattern);
		return routeDefinition;
	}

	public RouteSetBuilder add(RouteBuilder routeDefinition) {
		// TODO don't clone route definitions once they are separate from builder
		routeListBuilders.add(new SingleRouteDefinition(routeDefinition.clone()));
		return this;
	}

	public RouteSetBuilder add(Route route) {
		routeListBuilders.add(new SingleRoute(route));
		return this;
	}

	// TODO should return RouteBuilder
	public Route apply(UrlPattern pattern, Map<String, String> applyParameters) {
		HashMap<String, String> routeParameters = (HashMap<String, String>) routeDefinition.parameterValues.clone();
		routeParameters.putAll(applyParameters);

		UrlPattern appliedPattern = pattern.apply(applyParameters, routeDefinition.parameterValues);
		Route route = new Route();
		route.setUrlPattern(appliedPattern);
		route.setStaticParameters(routeParameters);
		route.setDefaultStaticParameters(routeDefinition.defaultStaticParameterValues);
		route.setName(routeDefinition.getName());
		route.setMethods(routeDefinition.getMethods());
		route.setExcludedMethods(routeDefinition.getExcludedMethods());

		add(route);

		return route;
	}

	public RouteSet createRouteSet() {
		ArrayList<Route> routes = new ArrayList<Route>();
		buildRouteList(routes);
		RouteSet routeSet = new RouteSet();
		routeSet.setRoutes(routes);
		for (Route route : routes) {
			System.out.println(route.getUrlPattern().getRegex());
		}
		return routeSet;
	}

	public RouteSetBuilder setTemplate(String name, RouteSetBuilderTemplate template) {
		templates.put(name, template);
		return this;
	}

	public RouteSetBuilder template(RouteSetBuilderTemplate template) {
		RouteSetBuilder result = nested();
		template.applyTemplate(result);
		return result;
	}

	public RouteSetBuilder template(String name) {
		// TODO throw exception if template isn't defined
		return template(templates.get(name));
	}

	public void buildRouteList(List<Route> routes) {
		for (RouteListBuilder routeListBuilder : routeListBuilders) {
			routeListBuilder.buildRouteList(routes);
		}
	}

	public RouteBuilder setOption(String optionName, String value) {
		return routeDefinition.setOption(optionName, value);
	}

	public String getOption(String name) {
		return routeDefinition.getOption(name);
	}

	public RouteBuilder setParameterValue(String name, String value) {
		return routeDefinition.setParameterValue(name, value);
	}

	public RouteBuilder setDefaultStaticParameterValue(String name, String value) {
		return routeDefinition.setDefaultStaticParameterValue(name, value);
	}

	public RouteBuilder setParameterRegex(String name, String regex) {
		return routeDefinition.setParameterRegex(name, regex);
	}

	public UrlPattern createUrlPattern() {
		return routeDefinition.createUrlPattern();
	}
}

interface RouteListBuilder {
	public void buildRouteList(List<Route> routes);
}

class SingleRouteDefinition implements RouteListBuilder {
	private RouteBuilder routeDefinition;

	SingleRouteDefinition(RouteBuilder routeDefinition) {
		this.routeDefinition = routeDefinition;
	}

	public void buildRouteList(List<Route> routes) {
		routes.add(routeDefinition.createRoute());
	}
}

class SingleRoute implements RouteListBuilder {
	private Route route;

	public SingleRoute(Route route) {
		this.route = route;
	}

	public void buildRouteList(List<Route> routes) {
		routes.add(route);
	}
}
