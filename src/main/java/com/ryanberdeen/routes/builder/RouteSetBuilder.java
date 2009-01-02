package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.List;

import com.ryanberdeen.routes.Route;
import com.ryanberdeen.routes.RouteSet;

public class RouteSetBuilder implements RouteListBuilder, RouteOptions {
	private ArrayList<RouteListBuilder> routeListBuilders = new ArrayList<RouteListBuilder>();

	private RouteBuilder routeDefinition;

	public RouteSetBuilder() {
		routeDefinition = new RouteBuilder();
	}

	public RouteSetBuilder(RouteSetBuilder that) {
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

	public RouteOptions add(Route route) {
		routeListBuilders.add(new SingleRoute(route));
		return this;
	}

	@Deprecated
	public RouteBuilder getRouteDefinition() {
		return routeDefinition;
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

	public void buildRouteList(List<Route> routes) {
		for (RouteListBuilder routeListBuilder : routeListBuilders) {
			routeListBuilder.buildRouteList(routes);
		}
	}

	public RouteBuilder setOption(String optionName, String value) {
		return routeDefinition.setOption(optionName, value);
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
