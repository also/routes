package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ryanberdeen.routes.Route;
import com.ryanberdeen.routes.RouteSet;

public class RouteSetBuilder implements RouteListBuilder, RouteOptions {
	private HashMap<String, RouteSetBuilderTemplate> templates = new HashMap<String, RouteSetBuilderTemplate>();
	private ArrayList<RouteListBuilder> routeListBuilders = new ArrayList<RouteListBuilder>();

	private RouteBuilder routeDefinition;

	public RouteSetBuilder() {
		routeDefinition = new RouteBuilder();
	}

	private RouteSetBuilder(RouteSetBuilder that) {
		templates = new HashMap<String, RouteSetBuilderTemplate>(that.templates);
		routeDefinition = new RouteBuilder(that.routeDefinition);
	}

	public RouteSetBuilder nested() {
		RouteSetBuilder nested = new RouteSetBuilder(this);
		routeListBuilders.add(nested);
		return nested;
	}

	public RouteBuilder match() {
		RouteBuilder result = new RouteBuilder(routeDefinition);
		routeListBuilders.add(new SingleRouteDefinition(result));
		return result;
	}

	public RouteBuilder match(String pattern) {
		RouteBuilder routeDefinition = match();
		routeDefinition.append(pattern);
		return routeDefinition;
	}

	public RouteSetBuilder append(String pattern) {
		routeDefinition.append(pattern);
		return this;
	}

	public RouteBuilder apply(Map<String, String> applyParameters) {
		return match().apply(applyParameters);
	}

	public RouteSet createRouteSet() {
		ArrayList<Route> routes = new ArrayList<Route>();
		buildRouteList(routes);
		RouteSet routeSet = new RouteSet();
		routeSet.setRoutes(routes);

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
