/* $Id$ */

package org.ry1.springframework.web.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class DefaultRoute {
	private static final char[] PATTERN_SPECIAL_CHARS = ".\\+*?[^]$(){}=!<>|:".toCharArray();
	private static final String VALUE_WITHOUT_SLASHES = "([^/]+)";
	private static final String VALUE_WITH_SLASHES = "(.+)";
	private static final char PARAMETER_WITHOUT_SLASHES_PREFIX = ':';
	private static final char PARAMETER_WITH_SLASHES_PREFIX = '*';
	private String name;
	private String pattern;
	private Pattern regex;
	private boolean hasUrlParameter;
	private ArrayList<UrlParameter> urlParameters;
	
	/** The parameters that are required to generate a URL for this route. */
	private ArrayList<String> requiredParameters;
	private HashMap<String, Object> defaultParameters;
	private Map<String, String> staticParameters;
	private Set<String> methods;
	private Set<String> excludedMethods;

	private DefaultRoute() {
		staticParameters = new HashMap<String, String>();
		
		defaultParameters = new HashMap<String, Object>();
		requiredParameters = new ArrayList<String>();
		urlParameters = new ArrayList<UrlParameter>();
	}
	
	/** Creates a new unnamed route.
	 * @param pattern the pattern the route will match
	 * @param staticParameters the parameters that will always be applied
	 */
	public DefaultRoute(String pattern, Map<String, String> staticParameters) {
		this(pattern, staticParameters, null);
	}
	
	/** Creates a new named route.
	 * @param pattern the pattern the route will match
	 * @param staticParameters the parameters that will always be applied
	 * @param name the name of the route
	 */
	public DefaultRoute(String pattern, Map<String, String> staticParameters, String name) {
		this(pattern, staticParameters, name, null, null);
	}
	
	public DefaultRoute(String pattern, Map<String, String> staticParameters, String name, Set<String> methods, Set<String> excludedMethods) {
		this();
		
		this.pattern = pattern;
		
		// clone the static parameters
		if (staticParameters != null) {
			this.staticParameters.putAll(staticParameters);
		}
		
		this.name = name;
		
		// if methods are empty, set to null
		this.methods = methods != null && methods.size() > 0 ? methods : null;
		this.excludedMethods = excludedMethods != null && excludedMethods.size() > 0 ? excludedMethods : null;
		
		boolean requireNameStart = false;
		StringBuilder nameBuilder = null;
		boolean allowSlashes = false;
		StringBuilder urlPartBuilder = new StringBuilder();
		
		for (int i = 0, len = pattern.length(); i < len; ++i) {
			char c = pattern.charAt(i);
			if (nameBuilder != null) {
				if (isValidNameChar(c)) {
					nameBuilder.append(c);
				}
				else {
					addParameterName(urlPartBuilder, nameBuilder, allowSlashes);
					urlPartBuilder = new StringBuilder();
					nameBuilder = null;
					
					//
					--i;
				}
			}
			else if (c == PARAMETER_WITHOUT_SLASHES_PREFIX) {
				allowSlashes = false;
				requireNameStart = true;
			}
			else if (c == PARAMETER_WITH_SLASHES_PREFIX) {
				allowSlashes = true;
				requireNameStart = true;
			}
			else {
				urlPartBuilder.append(c);
			}
			
			// peek ahead at the next name character and make sure there can be a valid name
			if (requireNameStart) {
				if (i == len - 1) {
					throw new IllegalArgumentException("Invalid pattern: expecting name, found end of pattern");
				}
				else {
					char d = pattern.charAt(i + 1);
					if (!isValidNameChar(d)) {
						throw new IllegalArgumentException("Invalid pattern: expecting name, found '" + pattern.substring(i + 1) + "' at index " + i);
					}
					else {
						hasUrlParameter = true;
						requireNameStart = false;
						nameBuilder = new StringBuilder();
						nameBuilder.append(d);
						++i;
					}
				}
			}
		}
		
		addParameterName(urlPartBuilder, nameBuilder, allowSlashes);
	}
	
	private void addParameterName(StringBuilder urlPartBuilder, StringBuilder parameterNameBuilder, boolean allowSlashes) {
		boolean required = false;

		String parameterName = null;
		if (parameterNameBuilder != null) {
			parameterName = parameterNameBuilder.toString();
			
			if (staticParameters.containsKey(parameterName)) {
				defaultParameters.put(parameterName, staticParameters.remove(parameterName));
			}
			else {
				requiredParameters.add(parameterName);
				required = true;
			}
		}
		
		urlParameters.add(new UrlParameter(required, allowSlashes, urlPartBuilder.toString(), parameterName));
	}
	
	private boolean isPatternSpecialChar(char c) {
		for (char special : PATTERN_SPECIAL_CHARS) {
			if (c == special) {
					return true;
			}
		}
		
		return false;
	}
	
	/** Tests if the character is a valid name character: [A-Za-z0-9_]
	 */
	private boolean isValidNameChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_'; 
	}
	
	/** Lazily create the regular expression.
	 */
	private Pattern getRegex() {
		if (regex == null) {
			StringBuilder regexBuilder = new StringBuilder();
			regexBuilder.append('^');
			
			for (UrlParameter urlParameter : urlParameters) {
				for (char c : urlParameter.precedingUrlPart.toCharArray()) {
					if (isPatternSpecialChar(c)) {
						regexBuilder.append('\\');
					}
					regexBuilder.append(c);
				}
				
				if (urlParameter.name != null) {
					regexBuilder.append(urlParameter.allowSlashes ? VALUE_WITH_SLASHES : VALUE_WITHOUT_SLASHES);
					if (!urlParameter.required) {
						regexBuilder.append('?');
					}
				}
			}
			
			regexBuilder.append('$');
			regex = Pattern.compile(regexBuilder.toString());
			System.out.println(regex);
		}
		
		return regex;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, String> getMatch(String url, HttpServletRequest request) {
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
			Matcher matcher = getRegex().matcher(url);
			if (matcher.matches()) {
				result = new HashMap<String, String>();
				
				int matchNumber = 1;
				for (UrlParameter urlParameter : urlParameters) {
					if (urlParameter.name != null) {
						String value = matcher.group(matchNumber++);
						if (value == null) {
							value = String.valueOf(defaultParameters.get(urlParameter.name));
						}
						result.put(urlParameter.name, value);
					}
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
	
	public String getUrl(Map<String, Object> parameters, Map<String, String> contextParameters) {
		StringBuilder urlBuilder = new StringBuilder();
		// FIXME assumes parameter is no first
		for (UrlParameter urlParameter : urlParameters) {
			urlBuilder.append(urlParameter.precedingUrlPart);
			if (urlParameter.name != null) {
				Object value = parameters.get(urlParameter.name);
				if (value == null) {
					value = defaultParameters.get(urlParameter.name);
				}
				if (value == null) {
					String contextValue = contextParameters.get(urlParameter.name);
					if (contextValue != null) {
						value = contextValue;
					}
				}
				if (urlParameter.required || !value.equals(defaultParameters.get(urlParameter.name))) {
					urlBuilder.append(value);
				}
			}
		}
		
		return urlBuilder.toString();
	}
	
	public String getUrlPattern() {
		HashMap<String, Object> parameterPatterns = new HashMap<String, Object>(urlParameters.size());
		for (UrlParameter parameter : urlParameters) {
			if (parameter.name != null) {
				parameterPatterns.put(parameter.name, "${" + parameter.name + '}');
			}
		}
		return getUrl(parameterPatterns, null);
	}
	
	public DefaultRoute apply(Map<String, String> parameters) {
		DefaultRoute result = new DefaultRoute();
		
		result.defaultParameters.putAll(defaultParameters);
		result.staticParameters.putAll(staticParameters);
		result.staticParameters.putAll(parameters);
		result.requiredParameters.addAll(requiredParameters);
		result.methods = methods;
		result.excludedMethods = excludedMethods;
		
		StringBuilder urlPartBuilder = new StringBuilder();
		for (UrlParameter urlParameter : urlParameters) {
			if (urlParameter.name != null) {
				urlPartBuilder.append(urlParameter.precedingUrlPart);
				String value = parameters.get(urlParameter.name);
				if (value != null) {
					urlPartBuilder.append(value);
					result.defaultParameters.remove(urlParameter.name);
					result.staticParameters.remove(urlParameter.name);
					result.requiredParameters.remove(urlParameter.name);
				}
				else {
					result.urlParameters.add(new UrlParameter(urlParameter.required, urlParameter.allowSlashes, urlPartBuilder.toString(), urlParameter.name));
					urlPartBuilder = new StringBuilder();
				}
			}
			else {
				result.urlParameters.add(urlParameter);
			}
		}
		
		return result;
	}
	
	private static class UrlParameter {
		private boolean required;
		private boolean allowSlashes;
		private String precedingUrlPart;
		private String name;

		public UrlParameter(boolean required, boolean allowSlashes, String precedingUrlPart, String name) {
			this.required = required;
			this.allowSlashes = allowSlashes;
			this.precedingUrlPart = precedingUrlPart;
			this.name = name;
		}
	}
}
