package com.ryanberdeen.routes;

import java.util.Collections;
import java.util.Map;

public interface Mapping {
	public Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

	public abstract Route getNamedRoute(String name);

	public Map<String, Route> getNamedRoutes();

	public RequestMatch getBestMatch(String method, String path);

	public Route getBestMatch(Map<String, Object> parameters);

	public Route getBestMatch(Map<String, Object> parameters, Map<String, String> contextParameters);

	public String getPath(String name, Map<String, Object> parameters, Map<String, String> contextParameters);

	public String getPath(Map<String, Object> parameters, Map<String, String> contextParameters);
}
