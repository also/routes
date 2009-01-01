package com.ryanberdeen.routes;

import java.util.Map;

public interface Mapping {
	public abstract Route getNamedRoute(String name);

	public Map<String, Route> getNamedRoutes();

	public UrlMatch getBestMatch(String method, String path);

	public Route getBestMatch(Map<String, Object> parameters);

	public Route getBestMatch(Map<String, Object> parameters, Map<String, String> contextParameters);

	public String getPath(String name, Map<String, Object> parameters, Map<String, String> contextParameters);

	public String getPath(Map<String, Object> parameters, Map<String, String> contextParameters);
}
