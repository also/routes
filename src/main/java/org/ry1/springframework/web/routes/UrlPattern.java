package org.ry1.springframework.web.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Matches and extracts parameters from URLs.
 * @author Ryan Berdeen
 *
 */
public class UrlPattern implements Cloneable {
	private static final char[] PATTERN_SPECIAL_CHARS = ".\\+*?[^]$(){}=!<>|:".toCharArray();
	private static final String VALUE_WITHOUT_SLASHES = "[^/]+";
	private static final String VALUE_WITH_SLASHES = ".+";
	private static final char PARAMETER_WITHOUT_SLASHES_PREFIX = ':';
	private static final char PARAMETER_WITH_SLASHES_PREFIX = '*';
	
	/** The regular expression this pattern uses to test URLs. */
	private Pattern regex;
	
	/** The parameter names this pattern will provide. */
	private HashSet<String> parameterNames;
	
	/** The URL segments that make up this pattern.
	 *  These segments are used to generate the regular expression and generate
	 *  URLs from parameters.
	 */
	private ArrayList<UrlSegment> segments;
	
	private UrlPattern() {
		segments = new ArrayList<UrlSegment>();
		parameterNames = new HashSet<String>();
	}
	
	public static UrlPattern parse(String pattern) {
		return parse(pattern, null);
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
		StringBuilder segmentBuilder = new StringBuilder();
		
		for (int i = 0, len = pattern.length(); i < len; ++i) {
			char c = pattern.charAt(i);
			if (nameBuilder != null) {
				if (isValidNameChar(c)) {
					nameBuilder.append(c);
				}
				else {
					builder.addParameterName(segmentBuilder, nameBuilder, allowSlashes);
					segmentBuilder = new StringBuilder();
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
				segmentBuilder.append(c);
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
		
		builder.addParameterName(segmentBuilder, nameBuilder, allowSlashes);
		
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
	
	private static void appendStringToRegex(String string, StringBuilder regexBuilder) {
		for (char c : string.toCharArray()) {
			if (isPatternSpecialChar(c)) {
				regexBuilder.append('\\');
			}
			regexBuilder.append(c);
		}
	}
	
	/** Tests if the character is a valid name character: [A-Za-z0-9_]
	 */
	private static boolean isValidNameChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_'; 
	}
	
	public HashSet<String> getParameterNames() {
		return parameterNames;
	}
	
	/** Lazily create the regular expression.
	 */
	public Pattern getRegex() {
		if (regex == null) {
			StringBuilder regexBuilder = new StringBuilder();
			regexBuilder.append('^');
			
			for (UrlSegment segment : segments) {
				segment.appendRegex(regexBuilder);
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
			for (UrlSegment segment : segments) {
				if (segment instanceof ParameterSegment) {
					ParameterSegment parameter = (ParameterSegment) segment;
					String value = parameter.value != null ? parameter.value : matcher.group(matchNumber++);
					result.put(parameter.name, value);
				}
			}
		}
		
		return result;
	}
	
	/** Builds a URL using the parameters.
	 */
	public String buildUrl(Map<String, Object> parameters, Map<String, Object> defaultParameters, Map<String, String> contextParameters) {
		StringBuilder urlBuilder = new StringBuilder();
		for (UrlSegment segment : segments) {
			segment.appendUrl(urlBuilder, parameters, defaultParameters, contextParameters);
		}
		
		return urlBuilder.toString();
	}
	
	/** Creates a template for substituting parameters. Parameters are represented
	 * as <code>${parameterName}</code>, similar to JSP EL.
	 */
	public String getStringTemplate() {
		StringBuilder templateBuilder = new StringBuilder();
		for (UrlSegment segment : segments) {
			segment.appendTemplate(templateBuilder);
		}
		
		return templateBuilder.toString();
	}
	
	public UrlPattern apply(Map<String, String> parameters) {
		UrlPattern result = new UrlPattern();
		
		for (UrlSegment segment : segments) {
			result.append(segment.apply(parameters));
		}
		
		return result;
	}
	
	/** Returns a new UrlPattern with the pattern appended to it.
	 *  All parameters in the appended pattern are required.
	 */
	public UrlPattern append(String pattern) {
		return append(pattern, null);
	}
	
	/** Returns a new UrlPattern with the pattern appended to it.
	 *  The named parameters in the appended pattern are not required.
	 */
	public UrlPattern append(String pattern, Set<String> optionalParameterNames) {
		return append(parse(pattern, optionalParameterNames));
	}
	
	/** Returns a new UrlPattern with the pattern appended to it.
	 */
	public UrlPattern append(UrlPattern urlPattern) {
		UrlPattern result = clone();

		Iterator<UrlSegment> iterator = urlPattern.segments.iterator();
		
		if (iterator.hasNext()) {
			UrlSegment current = iterator.next();
			
			result.append(current);
			
			while (iterator.hasNext()) {
				current = iterator.next();
				result.segments.add(current.clone());
				if (current instanceof ParameterSegment) {
					result.parameterNames.add(((ParameterSegment) current).name);
				}
			}
		}
		return result;
	}
	
	/** Returns a new UrlPattern with the static url segment appended to it.
	 */
	public UrlPattern appendStatic(String segment) {
		UrlSegment parameter = new StaticSegment(segment);
		return appended(parameter);
	}
	
	/** Returns a new UrlPattern with the parameter appended to it.
	 *  The parameter is required and does not allow slashes.
	 */
	public UrlPattern appendParameter(String parameterName) {
		return appendParameter(parameterName, false);
	}
	
	/** Returns a new UrlPattern with the parameter appended to it.
	 *  The parameter is required.
	 */
	public UrlPattern appendParameter(String parameterName, boolean allowSlashes) {
		ParameterSegment parameter = new ParameterSegment(true, allowSlashes, parameterName);
		return appended(parameter);
	}
	
	/** Returns a new UrlPattern with the UrlSegment appended.
	 */
	private UrlPattern appended(UrlSegment segment) {
		UrlPattern result = clone();
		result.append(segment);
		return result;
	}
	
	/** Appends a UrlSegment. Only call this method during pattern creation.
	 */
	private void append(UrlSegment segment) {
		segments.add(segment.clone());
		if (segment instanceof ParameterSegment) {
			ParameterSegment parameter = (ParameterSegment) segment;
			if (parameter.value == null) {
				parameterNames.add(parameter.name);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UrlPattern clone() {
		try {
			UrlPattern result = (UrlPattern) super.clone();
			
			result.segments = new ArrayList<UrlSegment>(segments.size());
			for (UrlSegment parameter : segments) {
				result.segments.add(parameter.clone());
			}
			
			result.parameterNames = (HashSet<String>) parameterNames.clone();
			
			return result;
		}
		catch (CloneNotSupportedException ex) {
			throw new Error(ex);
		}
	}
	
	private static interface UrlSegment extends Cloneable {
		/** Appends the regex that represents this segment to the builder.
		 */
		public void appendRegex(StringBuilder regexBuilder);
		
		/** Appends the URL generated by applying the parameters to this segment to the builder.
		 */
		public void appendUrl(StringBuilder urlBuilder, Map<String, Object> parameters, Map<String, Object> defaultParameters, Map<String, String> contextParameters);
		
		/** Appends the template that represents this segment to the builder.
		 */
		public void appendTemplate(StringBuilder templateBuilder);
		
		public UrlSegment apply(Map<String, String> parameters);
		
		public UrlSegment clone();
	}
	
	private static class StaticSegment implements UrlSegment, Cloneable {
		private String value;
		
		private StaticSegment(String value) {
			this.value = value;
		}
		
		public void appendRegex(StringBuilder regexBuilder) {
			appendStringToRegex(value, regexBuilder);
		}
		
		public void appendUrl(StringBuilder urlBuilder, Map<String, Object> parameters, Map<String, Object> defaultParameters, Map<String, String> contextParameters) {
			urlBuilder.append(value);
		}
		
		public void appendTemplate(StringBuilder templateBuilder) {
			templateBuilder.append(value);
		}
		
		public UrlSegment apply(Map<String, String> parameters) {
			return clone();
		}
		
		public StaticSegment clone() {
			return new StaticSegment(value);
		}
	}
	
	private static class ParameterSegment implements UrlSegment, Cloneable {
		private boolean required;
		private boolean allowSlashes;
		private String name;
		private String regex;
		private String value;

		public ParameterSegment(boolean required, boolean allowSlashes, String name) {
			this(required, allowSlashes, name, null);
		}

		public ParameterSegment(boolean required, boolean allowSlashes, String name, String regex) {
			this(required, allowSlashes, name, regex, null);
		}

		public ParameterSegment(boolean required, boolean allowSlashes, String name, String regex, String value) {
			this.required = required;
			this.allowSlashes = allowSlashes;
			this.name = name;
			this.regex = regex;
			this.value = value;
		}
		
		@Override
		public ParameterSegment clone() {
			try {
				return (ParameterSegment) super.clone();
			}
			catch (CloneNotSupportedException ex) {
				throw new Error(ex);
			}
		}
		
		public void appendRegex(StringBuilder regexBuilder) {
			regexBuilder.append('(');
			if (regex != null) {
				regexBuilder.append(regex);
			}
			else {
				regexBuilder.append(allowSlashes ? VALUE_WITH_SLASHES : VALUE_WITHOUT_SLASHES);
			}
			if (!required) {
				regexBuilder.append("|$");
			}
			regexBuilder.append(')');
		}
		
		public void appendUrl(StringBuilder urlBuilder, Map<String, Object> parameters, Map<String, Object> defaultParameters, Map<String, String> contextParameters) {
			Object result = value;
			if (result == null) {
				result = parameters.get(name);
				if (result == null) {
					result = defaultParameters.get(name);
					if (result == null) {
						String contextValue = contextParameters.get(name);
						if (contextValue != null) {
							result = contextValue;
						}
					}
				}
			}
			
			if (required || !result.equals(defaultParameters.get(name))) {
				if (result == null) {
					throw new RuntimeException("No value for [" + name + "]");
				}
				urlBuilder.append(result);
			}
		}
		
		public UrlSegment apply(Map<String, String> parameters) {
			String value = parameters.get(name);
			if (value != null) {
				StringBuilder valueRegexBuilder = new StringBuilder();
				appendStringToRegex(value, valueRegexBuilder);
				return new ParameterSegment(true, false, name, valueRegexBuilder.toString(), value);
			}
			else {
				return clone();
			}
		}
		
		public void appendTemplate(StringBuilder templateBuilder) {
			templateBuilder.append("${");
			templateBuilder.append(name);
			templateBuilder.append("}");
		}
	}
	
	private static class UrlPatternBuilder {
		private Set<String> optionalParameterNames;
		private UrlPattern urlPattern;
		
		public UrlPatternBuilder(Set<String> optionalParameterNames) {
			urlPattern = new UrlPattern();
			this.optionalParameterNames = optionalParameterNames;
		}
		
		public void addParameterName(Object segment, Object parameterName, boolean allowSlashes) {
			boolean required = true;
			String paramaterNameString = null;
			
			if (parameterName != null) {
				paramaterNameString = parameterName.toString();
				
				urlPattern.parameterNames.add(paramaterNameString);
				
				if (optionalParameterNames == null || optionalParameterNames.contains(paramaterNameString)) {
					required = false;
				}
			}
			
			urlPattern.segments.add(new StaticSegment(segment.toString()));
			
			if (paramaterNameString != null && !"".equals(paramaterNameString)) {
				urlPattern.segments.add(new ParameterSegment(required, allowSlashes, paramaterNameString));
			}
		}
		
		public UrlPattern getUrlPattern() {
			return urlPattern;
		}
	}
}
