package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.List;

import com.ryanberdeen.routes.Route;
import com.ryanberdeen.routes.RouteSet;

public class RouteSetBuilder implements RouteListBuilder {
	private ArrayList<RouteListBuilder> routeListBuilders = new ArrayList<RouteListBuilder>();

	public RouteBuilder match(String pattern) {
		RouteBuilder routeDefinition = new RouteBuilder();
		routeDefinition.setPattern(pattern);
		add(routeDefinition);
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

	public RouteSet createRouteSet() {
		ArrayList<Route> routes = new ArrayList<Route>();
		buildRouteList(routes);
		RouteSet routeSet = new RouteSet();
		routeSet.setRoutes(routes);
		return routeSet;
	}

	public void buildRouteList(List<Route> routes) {
		for (RouteListBuilder routeListBuilder : routeListBuilders) {
			routeListBuilder.buildRouteList(routes);
		}
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
