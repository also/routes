package com.ryanberdeen.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ryanberdeen.routes.builder.PathPatternBuilder;

public class Route {
	public static final Map<String, String> NO_PARAMETER_VALUES = Collections.emptyMap();
	
	private String name;
	private Map<String, String> staticParameterValues;
	private Map<String, String> defaultStaticParameterValues;
	
	/** The parameters that are required to generate a URL for this route.
	 * These parameters must be present in {@link #match(Map, Map)} for the 
	 * route to match. */
	private ArrayList<String> requiredUrlParameterNames;
	private HashMap<String, String> requiredStaticParameterValues;
	private HashMap<String, String> optionalStaticParameterValues;
	private Set<String> methods;
	private Set<String> excludedMethods;
	private UrlPattern urlPattern;

	public Route() {
		staticParameterValues = NO_PARAMETER_VALUES;
		defaultStaticParameterValues = NO_PARAMETER_VALUES;
	}

	/** Creates a new unnamed route.
	 * @param pattern the pattern the route will match
	 * @param staticParameters the parameters that will be applied
	 */
	public Route(String pattern, Map<String, String> staticParameters, Map<String, String> parameterRegexes) {
		this();
		this.urlPattern = PathPatternBuilder.parse(pattern, staticParameters.keySet(), parameterRegexes);
		this.staticParameterValues = staticParameters;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setUrlPattern(UrlPattern urlPattern) {
		this.urlPattern = urlPattern;
	}

	/** Sets the route's static parameters. For parameters that are also contained in the
	 * URL pattern, the values here will be defaults. The rest will always be
	 * applied.
	 */
	public void setStaticParameters(Map<String, String> staticParameters) {
		this.staticParameterValues = staticParameters;
	}
	
	/** Set the default static parameters. Default these defaults are included in the
	 * applied parameters, but are overridden by static parameters. If a default static
	 * parameter has the same value as a static parameter, the static parameter will not
	 * be required for a parameter match.
	 * @param defaultStaticParameters
	 */
	public void setDefaultStaticParameters(Map<String, String> defaultStaticParameters) {
		this.defaultStaticParameterValues = defaultStaticParameters;
	}

	/** Sets the allowed methods. The default allows any method.
	 */
	public void setMethods(Set<String> methods) {
		this.methods = methods != null && methods.size() > 0 ? methods : null;
	}

	/** Sets the forbidden methods. The default allows any method.
	 */
	public void setExcludedMethods(Set<String> excludedMethods) {
		this.excludedMethods = excludedMethods != null && excludedMethods.size() > 0 ? excludedMethods : null;
	}

	/** Determines what parameters are required based on the route's parameters
	 * and the URL's parameters.
	 */
	public void prepare() {
		requiredStaticParameterValues = new HashMap<String, String>(staticParameterValues);
		optionalStaticParameterValues = new HashMap<String, String>();
		
		requiredUrlParameterNames = new ArrayList<String>();

		for (String parameterName : urlPattern.getParameterNames()) {
			// parameters that occur in the URL don't have a required static value
			String value = requiredStaticParameterValues.remove(parameterName);
			
			// parameter that occur in the URL but not in the static parameters or default must have a value
			if (value == null && !defaultStaticParameterValues.containsKey(parameterName)) {
				requiredUrlParameterNames.add(parameterName);
			}
		}
		
		// static parameters that have the default value aren't required
		for (Map.Entry<String, String> defaultStaticParameter : defaultStaticParameterValues.entrySet()) {
			String parameterName = defaultStaticParameter.getKey();
			String defaultParameterValue = defaultStaticParameter.getValue();
			
			if (defaultParameterValue.equals(requiredStaticParameterValues.get(parameterName))) {
				requiredStaticParameterValues.remove(defaultStaticParameter.getKey());
				optionalStaticParameterValues.put(parameterName, defaultParameterValue);
			}
		}
	}

	public String getName() {
		return name;
	}

	/** Matches the URL and request against the route. The URL must match the
	 * URL pattern, and the request {@link HttpServletRequest#getMethod() method}
	 * must be included an allowed and not excluded method.
	 */
	public Map<String, String> match(String url, String method) {
		if (methods != null && !methods.contains(method)) {
			return null;
		}
		if (excludedMethods != null && methods.contains(method)) {
			return null;
		}

		Map<String, String> result = null;

		Map<String, String> urlMatches = urlPattern.match(url);
		if (urlMatches != null) {
			result = new HashMap<String, String>(defaultStaticParameterValues);
			result.putAll(staticParameterValues);
			result.putAll(urlMatches);
		}

		return result;
	}

	/** Matches parameters against the parameters of the route. The parameters
	 * must include all required parameters, and all static parameters must
	 * have the correct values.
	 * @return the number of matched parameters, or -1 for no match
	 */
	public int match(Map<String, Object> parameters, Map<String, String> contextParameters) {
		// make sure all required url parameters are provided
		for (String requiredParameter : requiredUrlParameterNames) {
			if (!parameters.containsKey(requiredParameter) && !contextParameters.containsKey(requiredParameter)) {
				return -1;
			}
		}

		int matchCount = requiredUrlParameterNames.size();

		// make sure all static parameters match
		for (Map.Entry<String, String> staticParameter : requiredStaticParameterValues.entrySet()) {
			String key = staticParameter.getKey();
			Object parameterValue = parameters.get(key);
			if (parameterValue == null) {
				parameterValue = contextParameters.get(key);
			}
			
			if (!staticParameter.getValue().equals(parameterValue)) {
				return -1;
			}

			matchCount++;
		}
		
		for (Map.Entry<String, String> optionalStaticParameter: optionalStaticParameterValues.entrySet()) {
			String key = optionalStaticParameter.getKey();
			Object parameterValue = parameters.get(key);
			if (parameterValue == null) {
				parameterValue = contextParameters.get(key);
			}
			
			if (parameterValue != null) {
				if (!optionalStaticParameter.getValue().equals(parameterValue)) {
					return -1;
				}
	
				matchCount++;
			}
		}

		return matchCount;
	}

	public Route apply(Map<String, String> parameters, Set<String> methods, Set<String> excludedMethods) {
		Route result = new Route();

		result.urlPattern = urlPattern.apply(parameters, staticParameterValues);
		result.staticParameterValues = new HashMap<String, String>(this.staticParameterValues);
		result.staticParameterValues.putAll(parameters);
		result.prepare();
		return result;
	}

	public String buildUrl(Map<String, Object> parameters, Map<String, String> contextParameters) {
		return urlPattern.buildUrl(parameters, staticParameterValues, contextParameters);
	}

	public UrlPattern getUrlPattern() {
		return urlPattern;
	}
}
