/* $Id$ */

package org.ry1.springframework.web.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class Route {
	public static final Map<String, String> NO_PARAMETER_VALUES = Collections.emptyMap();
	
	private String name;
	
	/** The parameters that are required to generate a URL for this route.
	 * These parameters must be present in {@link #match(Map, Map)} for the 
	 * route to match. */
	private ArrayList<String> requiredUrlParameters;
	private Map<String, String> staticParameters;
	private Map<String, String> defaultStaticParameters;
	private HashMap<String, String> defaultParameters;
	private HashMap<String, String> requiredStaticParameters;
	private Set<String> methods;
	private Set<String> excludedMethods;
	private UrlPattern urlPattern;

	public Route() {
		staticParameters = NO_PARAMETER_VALUES;
		defaultStaticParameters = NO_PARAMETER_VALUES;
	}

	/** Creates a new unnamed route.
	 * @param pattern the pattern the route will match
	 * @param staticParameters the parameters that will be applied
	 */
	public Route(String pattern, Map<String, String> staticParameters, Map<String, String> parameterRegexes) {
		this();
		this.urlPattern = UrlPattern.parse(pattern, staticParameters.keySet(), parameterRegexes);
		this.staticParameters = staticParameters;
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
		this.staticParameters = staticParameters;
	}
	
	/** Set the default static parameters. Default these defaults are included in the
	 * applied parameters, but are overridden by static parameters. If a default static
	 * parameter has the same value as a static parameter, the static parameter will not
	 * be required for a parameter match.
	 * @param defaultStaticParameters
	 */
	public void setDefaultStaticParameters(Map<String, String> defaultStaticParameters) {
		this.defaultStaticParameters = defaultStaticParameters;
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
		requiredStaticParameters = new HashMap<String, String>(staticParameters);

		defaultParameters = new HashMap<String, String>();
		requiredUrlParameters = new ArrayList<String>();

		for (String parameterName : urlPattern.getParameterNames()) {
			String value = requiredStaticParameters.remove(parameterName);
			
			if (value != null) {
				defaultParameters.put(parameterName, value);
			}
			else if (!defaultStaticParameters.containsKey(parameterName)) {
				requiredUrlParameters.add(parameterName);
			}
		}
		
		// static parameters that have the default value aren't required
		for (Map.Entry<String, String> defaultStaticParameter : defaultStaticParameters.entrySet()) {
			if (defaultStaticParameter.getValue().equals(requiredStaticParameters.get(defaultStaticParameter.getKey()))) {
				requiredStaticParameters.remove(defaultStaticParameter.getKey());
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
	public Map<String, String> match(String url, HttpServletRequest request) {
		if (methods != null && !methods.contains(request.getMethod())) {
			return null;
		}
		if (excludedMethods != null && methods.contains(request.getMethod())) {
			return null;
		}

		Map<String, String> result = null;

		Map<String, String> urlMatches = urlPattern.match(url);
		if (urlMatches != null) {
			result = new HashMap<String, String>(defaultStaticParameters);
			result.putAll(staticParameters);
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
		for (String requiredParameter : requiredUrlParameters) {
			if (!parameters.containsKey(requiredParameter) && !contextParameters.containsKey(requiredParameter)) {
				return -1;
			}
		}

		int matchCount = requiredUrlParameters.size();

		// make sure all static parameters match
		for (Map.Entry<String, String> staticParameter : requiredStaticParameters.entrySet()) {
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

		return matchCount;
	}

	public Route apply(Map<String, String> parameters, Set<String> methods, Set<String> excludedMethods) {
		Route result = new Route();

		result.urlPattern = urlPattern.apply(parameters, defaultParameters);
		result.staticParameters = new HashMap<String, String>(this.staticParameters);
		result.staticParameters.putAll(parameters);
		result.prepare();
		return result;
	}

	public String buildUrl(Map<String, Object> parameters, Map<String, String> contextParameters) {
		return urlPattern.buildUrl(parameters, defaultParameters, contextParameters);
	}

	public UrlPattern getUrlPattern() {
		return urlPattern;
	}
}
