/* $Id$ */

package org.ry1.springframework.web.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class DefaultRoute {
	private String name;
	private String pattern;
	private boolean hasUrlParameter;

	/** The parameters that are required to generate a URL for this route. */
	private ArrayList<String> requiredParameters;
	private Map<String, String> parameters;
	private HashMap<String, Object> defaultParameters;
	private HashMap<String, String> staticParameters;
	private Set<String> methods;
	private Set<String> excludedMethods;
	private UrlPattern urlPattern;

	public DefaultRoute() {

	}

	/** Creates a new unnamed route.
	 * @param pattern the pattern the route will match
	 * @param parameters the parameters that will be applied
	 */
	public DefaultRoute(String pattern, Map<String, String> parameters) {
		this(pattern, parameters, null);
	}

	/** Creates a new named route.
	 * @param pattern the pattern the route will match
	 * @param parameters the parameters that will be applied
	 * @param name the name of the route
	 */
	public DefaultRoute(String pattern, Map<String, String> parameters, String name) {
		this(pattern, parameters, name, null, null);
	}

	public DefaultRoute(String pattern, Map<String, String> parameters, String name, Set<String> methods, Set<String> excludedMethods) {
		this(UrlPattern.parse(pattern, parameters.keySet()), parameters, name, methods, excludedMethods);

		this.pattern = pattern;
	}

	public DefaultRoute(UrlPattern urlPattern, Map<String, String> parameters, String name, Set<String> methods, Set<String> excludedMethods) {
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

			defaultParameters = new HashMap<String, Object>();
			requiredParameters = new ArrayList<String>();

			hasUrlParameter = urlPattern.hasParameter();
			for (String parameterName : urlPattern.getParameterNames()) {
				if (staticParameters.containsKey(parameterName)) {
					defaultParameters.put(parameterName, staticParameters.remove(parameterName));
				}
				else {
					requiredParameters.add(parameterName);
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public Map<String, String> match(String url, HttpServletRequest request) {
		if (methods != null && !methods.contains(request.getMethod())) {
			return null;
		}
		if (excludedMethods != null && methods.contains(request.getMethod())) {
			return null;
		}

		Map<String, String> result = null;
		if (!hasUrlParameter) {
			if (url.equals(pattern)) {
				result = staticParameters;
			}
		}
		else {
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
		}

		return result;
	}

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
				String value = contextParameters.get(key);
				if (value != null) {
					parameterValue = value;
				}
			}
			if (parameterValue != null) {
				if (!parameterValue.equals(staticParameter.getValue())) {
					return -1;
				}

				matchCount++;
			}
		}

		return matchCount;
	}

	public DefaultRoute apply(Map<String, String> parameters, Set<String> methods, Set<String> excludedMethods) {
		DefaultRoute result = new DefaultRoute();

		result.urlPattern = urlPattern.apply(parameters);
		result.parameters = new HashMap<String, String>(this.parameters);
		result.parameters.putAll(parameters);
		result.prepareParameters();

		return result;
	}

	public String getUrl(Map<String, Object> parameters, Map<String, String> contextParameters) {
		return urlPattern.getUrl(parameters, defaultParameters, contextParameters);
	}

	public UrlPattern getUrlPattern() {
		return urlPattern;
	}
}
