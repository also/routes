/* $Id$ */

package org.ry1.springframework.web.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class Route {
	private String name;

	/** The parameters that are required to generate a URL for this route.
	 * These parameters must be present in {@link #match(Map, Map)} for the 
	 * route to match. */
	private ArrayList<String> requiredParameters;
	private Map<String, String> parameters;
	private HashMap<String, String> defaultParameters;
	private HashMap<String, String> staticParameters;
	private Set<String> methods;
	private Set<String> excludedMethods;
	private UrlPattern urlPattern;

	public Route() {

	}

	/** Creates a new unnamed route.
	 * @param pattern the pattern the route will match
	 * @param parameters the parameters that will be applied
	 */
	public Route(String pattern, Map<String, String> parameters) {
		this(pattern, parameters, null);
	}

	/** Creates a new named route.
	 * @param pattern the pattern the route will match
	 * @param parameters the parameters that will be applied
	 * @param name the name of the route
	 */
	public Route(String pattern, Map<String, String> parameters, String name) {
		this(pattern, parameters, name, null, null);
	}

	public Route(String pattern, Map<String, String> parameters, String name, Set<String> methods, Set<String> excludedMethods) {
		this(UrlPattern.parse(pattern, parameters.keySet()), parameters, name, methods, excludedMethods);
	}

	public Route(UrlPattern urlPattern, Map<String, String> parameters, String name, Set<String> methods, Set<String> excludedMethods) {
		this.urlPattern = urlPattern;

		this.parameters = parameters;

		this.name = name;

		// if methods are empty, set to null
		this.methods = methods != null && methods.size() > 0 ? methods : null;
		this.excludedMethods = excludedMethods != null && excludedMethods.size() > 0 ? excludedMethods : null;

		prepareParameters();
	}

	public void setUrlPattern(UrlPattern urlPattern) {
		this.urlPattern = urlPattern;
		prepareParameters();
	}

	/** Sets the route's parameters. For parameters that are also contained in the
	 * URL pattern, the values here will be defaults. The rest will always be
	 * applied.
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		prepareParameters();
	}

	/** Sets the allowed methods. The default allows any method.
	 */
	public void setMethods(Set<String> methods) {
		this.methods = methods;
	}

	/** Sets the forbidden methods. The default allows any method.
	 */
	public void setExcludedMethods(Set<String> excludedMethods) {
		this.excludedMethods = excludedMethods;
	}

	/** Determines what parameters are required based on the route's parameters
	 * and the URL's parameters.
	 */
	private void prepareParameters() {
		if (urlPattern != null && parameters != null) {
			staticParameters = new HashMap<String, String>(parameters);

			defaultParameters = new HashMap<String, String>();
			requiredParameters = new ArrayList<String>();

			for (String parameterName : urlPattern.getParameterNames()) {
				if (staticParameters.containsKey(parameterName)) {
					defaultParameters.put(parameterName, staticParameters.remove(parameterName));
				}
				else {
					requiredParameters.add(parameterName);
				}
			}
			
			defaultParameters.putAll(urlPattern.getStaticParameters());
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
			result = new HashMap<String, String>();
			for (Map.Entry<String, String> urlMatch: urlMatches.entrySet()) {
				String key = urlMatch.getKey();
				Object value = urlMatch.getValue();
				if (value == null) {
					value = defaultParameters.get(key);
				}
				result.put(key, String.valueOf(value));
			}

			result.putAll(staticParameters);
		}

		return result;
	}

	/** Matches parameters against the parameters of the route. The parameters
	 * must include all required parameters, and all static parameters must
	 * have the correct values.
	 * @param parameters
	 * @param contextParameters
	 * @return the number of matched parameters, or -1 for no match
	 */
	public int match(Map<String, Object> parameters, Map<String, String> contextParameters) {
		// make sure all required parameters are provided
		for (String requiredParameter : requiredParameters) {
			if (!parameters.containsKey(requiredParameter) && !contextParameters.containsKey(requiredParameter)) {
				return -1;
			}
		}

		int matchCount = requiredParameters.size();

		// make sure all static parameters match
		for (Map.Entry<String, String> staticParameter : staticParameters.entrySet()) {
			String key = staticParameter.getKey();
			Object parameterValue = parameters.get(key);
			if (parameterValue == null) {
				parameterValue = contextParameters.get(key);
				if (parameterValue == null) {
					parameterValue = defaultParameters.get(key);
				}
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
		result.parameters = new HashMap<String, String>(this.parameters);
		result.parameters.putAll(parameters);
		result.prepareParameters();
		return result;
	}

	public String buildUrl(Map<String, Object> parameters, Map<String, String> contextParameters) {
		return urlPattern.buildUrl(parameters, defaultParameters, contextParameters);
	}

	public UrlPattern getUrlPattern() {
		return urlPattern;
	}
}
