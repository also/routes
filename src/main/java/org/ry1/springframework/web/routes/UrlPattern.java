package org.ry1.springframework.web.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Matches and extracts parameters from URLs.
 * @author Ryan Berdeen
 *
 */
public class UrlPattern {
	private static final char[] PATTERN_SPECIAL_CHARS = ".\\+*?[^]$(){}=!<>|:".toCharArray();
	private static final String VALUE_WITHOUT_SLASHES = "([^/]+)";
	private static final String VALUE_WITH_SLASHES = "(.+)";
	private static final char PARAMETER_WITHOUT_SLASHES_PREFIX = ':';
	private static final char PARAMETER_WITH_SLASHES_PREFIX = '*';
	
	private Pattern regex;
	private HashSet<String> parameterNames;
	private ArrayList<UrlParameter> urlParameters;
	private boolean hasParameter;
	
	private UrlPattern() {
		urlParameters = new ArrayList<UrlParameter>();
		parameterNames = new HashSet<String>();
	}
	
	/** Parses a String into a URL pattern.
	 * 
	 * Parameters whose name is contained in <code>optionalParameterNames</code>
	 * will not be required for URLs to match.
	 * @param pattern describes the format of matching URLs
	 * @param optionalParameterNames parameter names that are not required
	 * @return a {@link UrlPattern} that will match URLs to the pattern
	 */
	public static UrlPattern parse(String pattern, Set<String> optionalParameterNames) {
		UrlPatternBuilder builder = new UrlPatternBuilder(optionalParameterNames);
		
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
					builder.addParameterName(urlPartBuilder, nameBuilder, allowSlashes);
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
						requireNameStart = false;
						nameBuilder = new StringBuilder();
						nameBuilder.append(d);
						++i;
					}
				}
			}
		}
		
		builder.addParameterName(urlPartBuilder, nameBuilder, allowSlashes);
		
		return builder.getUrlPattern();
	}
	
	private static boolean isPatternSpecialChar(char c) {
		for (char special : PATTERN_SPECIAL_CHARS) {
			if (c == special) {
					return true;
			}
		}
		
		return false;
	}
	
	/** Tests if the character is a valid name character: [A-Za-z0-9_]
	 */
	private static boolean isValidNameChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_'; 
	}
	
	public boolean hasParameter() {
		return hasParameter;
	}
	
	public HashSet<String> getParameterNames() {
		return parameterNames;
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
		}
		
		return regex;
	}
	
	/** Matches the pattern against the URL. If the URL matches, a map of parameter
	 * values is returned. <code>null</code> indicates no match.
	 * @param url the URL to test
	 * @return the parameters extracted from the URL if it matches, <code>null</code> otherwise
	 */
	public Map<String, String> match(String url) {
		HashMap<String, String> result = null;
		Matcher matcher = getRegex().matcher(url);
		if (matcher.matches()) {
			result = new HashMap<String, String>();
			
			int matchNumber = 1;
			for (UrlParameter urlParameter : urlParameters) {
				if (urlParameter.name != null) {
					String value = matcher.group(matchNumber++);
					result.put(urlParameter.name, value);
				}
			}
		}
		
		return result;
	}
	
	public String getUrl(Map<String, Object> parameters, Map<String, Object> defaultParameters, Map<String, String> contextParameters) {
		StringBuilder urlBuilder = new StringBuilder();
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
	
	/** Creates a template for substituting parameters. Parameters are represented
	 * as <code>${parameterName}</code>, similar to JSP EL.
	 */
	public String getStringTemplate() {
		StringBuilder urlBuilder = new StringBuilder();
		for (UrlParameter urlParameter : urlParameters) {
			urlBuilder.append(urlParameter.precedingUrlPart);
			if (urlParameter.name != null) {
				urlBuilder.append("${");
				urlBuilder.append(urlParameter.name);
				urlBuilder.append("}");
			}
		}
		
		return urlBuilder.toString();
	}
	
	public UrlPattern apply(Map<String, String> parameters) {
		UrlPattern result = new UrlPattern();
		
		StringBuilder urlPartBuilder = new StringBuilder();
		for (UrlParameter urlParameter : urlParameters) {
			if (urlParameter.name != null) {
				urlPartBuilder.append(urlParameter.precedingUrlPart);
				String value = parameters.get(urlParameter.name);
				if (value != null) {
					urlPartBuilder.append(value);
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
	
	private static class UrlPatternBuilder {
		private Set<String> optionalParameterNames;
		private UrlPattern urlPattern;
		
		public UrlPatternBuilder(Set<String> optionalParameterNames) {
			urlPattern = new UrlPattern();
			this.optionalParameterNames = optionalParameterNames;
		}
		
		public void addParameterName(Object urlPart, Object parameterName, boolean allowSlashes) {
			boolean required = false;
			String paramaterNameString = null;
			
			if (parameterName != null) {
				urlPattern.hasParameter = true;
				paramaterNameString = parameterName.toString();
				
				urlPattern.parameterNames.add(paramaterNameString);
				
				if (!optionalParameterNames.contains(paramaterNameString)) {
					required = true;
				}
			}
			
			urlPattern.urlParameters.add(new UrlParameter(required, allowSlashes, urlPart.toString(), paramaterNameString));
		}
		
		public UrlPattern getUrlPattern() {
			return urlPattern;
		}
	}
}
