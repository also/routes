package com.ryanberdeen.routes.path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Matches and extracts parameters from paths.
 * @author Ryan Berdeen
 *
 */
public class PathPattern {
	private static final char[] PATTERN_SPECIAL_CHARS = ".\\+*?[^]$(){}=!<>|:".toCharArray();

	/** The regular expression this pattern uses to test paths. */
	private Pattern regex;

	/** The parameter names this pattern will provide. */
	private Set<String> parameterNames;

	/** The path segments that make up this pattern.
	 *  These segments are used to generate the regular expression and generate
	 *  paths from parameters.
	 */
	private List<PathSegment> pathSegments;

	public PathPattern(List<PathSegment> pathSegments, Set<String> parameterNames) {
		this.pathSegments = pathSegments;
		this.parameterNames = parameterNames;
	}

	private static boolean isPatternSpecialChar(char c) {
		for (char special : PATTERN_SPECIAL_CHARS) {
			if (c == special) {
					return true;
			}
		}

		return false;
	}

	static void appendStringToRegex(String string, StringBuilder regexBuilder) {
		for (int i = 0, len = string.length(); i < len; i++) {
			char c = string.charAt(i);
			if (isPatternSpecialChar(c)) {
				regexBuilder.append('\\');
			}
			if (c == '/' && i == len - 1) {
				regexBuilder.append("(?:");
				regexBuilder.append(c);
				regexBuilder.append("|$)");
			}
			else {
				regexBuilder.append(c);
			}
		}
	}

	/** Returns the set of parameter names included in the path.
	 */
	public Set<String> getParameterNames() {
		return parameterNames;
	}

	public List<PathSegment> getPathSegments() {
		return pathSegments;
	}

	/** Lazily create the regular expression.
	 */
	public Pattern getRegex() {
		if (regex == null) {
			StringBuilder regexBuilder = new StringBuilder();
			regexBuilder.append('^');

			for (PathSegment segment : pathSegments) {
				segment.appendRegex(regexBuilder);
			}

			regexBuilder.append('$');
			regex = Pattern.compile(regexBuilder.toString());
		}

		return regex;
	}

	/** Matches the pattern against the path. If the path matches, a map of parameter
	 * values is returned. <code>null</code> indicates no match.
	 * @param path the path to test
	 * @return the parameters extracted from the path if it matches, <code>null</code> otherwise
	 */
	public Map<String, String> match(String path) {
		HashMap<String, String> result = null;
		Matcher matcher = getRegex().matcher(path);
		if (matcher.matches()) {
			result = new HashMap<String, String>();

			int matchNumber = 1;
			for (PathSegment segment : pathSegments) {
				if (segment instanceof ParameterSegment) {
					ParameterSegment parameter = (ParameterSegment) segment;
					String value = matcher.group(matchNumber++);

					if (!value.equals("")) {
						result.put(parameter.getName(), value);
					}
				}
			}
		}

		return result;
	}

	/** Builds a path using the parameters.
	 */
	public String buildPath(Map<String, Object> parameters, Map<String, String> staticParameterValues, Map<String, String> contextParameters) {
		PathBuilder pathBuilder = new PathBuilder();
		for (PathSegment segment : pathSegments) {
			segment.appendPath(pathBuilder, parameters, staticParameterValues, contextParameters);
		}

		return pathBuilder.toString();
	}

	/** Creates a template for substituting parameters. Parameters are represented
	 * as <code>${parameterName}</code>, similar to JSP EL.
	 */
	public String getStringTemplate() {
		StringBuilder templateBuilder = new StringBuilder();
		for (PathSegment segment : pathSegments) {
			segment.appendTemplate(templateBuilder);
		}

		return templateBuilder.toString();
	}
}
