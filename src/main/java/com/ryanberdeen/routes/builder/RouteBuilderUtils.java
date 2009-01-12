package com.ryanberdeen.routes.builder;

import java.util.Map;

import com.ryanberdeen.routes.Route;

public class RouteBuilderUtils {
	public static Route buildRoute(String pathPatternString, Map<String, String> staticParameterValues) {
		return buildRoute(pathPatternString, staticParameterValues, Route.NO_PARAMETER_VALUES);
	}

	public static Route buildRoute(String pathPatternString, Map<String, String> staticParameterValues, Map<String, String> parameterRegexes) {
		RouteBuilder routeBuilder = new RouteBuilder();
		routeBuilder.append(pathPatternString);
		routeBuilder.setParameterValues(staticParameterValues);
		routeBuilder.setParameterRegexes(parameterRegexes);

		return routeBuilder.createRoute();
	}
}
