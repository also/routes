package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ryanberdeen.routes.PathPattern;
import com.ryanberdeen.routes.PathPattern.ParameterSegment;
import com.ryanberdeen.routes.PathPattern.StaticSegment;
import com.ryanberdeen.routes.PathPattern.PathSegment;

public class PathPatternBuilder implements Cloneable {
	private static final char PARAMETER_WITHOUT_SLASHES_PREFIX = ':';
	private static final char PARAMETER_WITH_SLASHES_PREFIX = '*';

	private Set<String> optionalParameterNames;
	private Map<String, String> parameterRegexes;

	private HashSet<String> parameterNames;
	private ArrayList<PathSegment> pathSegments;

	public PathPatternBuilder() {
		pathSegments = new ArrayList<PathSegment>();
		parameterNames = new HashSet<String>();
	}

	public PathPatternBuilder(Set<String> optionalParameterNames, Map<String, String> parameterRegexes) {
		this();
		this.optionalParameterNames = optionalParameterNames;
		this.parameterRegexes = parameterRegexes;
	}

	public void addParameterName(Object segment, Object parameterName, boolean allowSlashes) {
		boolean required = true;
		String paramaterNameString = null;
		String regex = null;

		if (parameterName != null) {
			paramaterNameString = parameterName.toString();

			parameterNames.add(paramaterNameString);

			if (optionalParameterNames != null && optionalParameterNames.contains(paramaterNameString)) {
				required = false;
			}

			if (parameterRegexes != null) {
				regex = parameterRegexes.get(paramaterNameString);
			}
		}

		pathSegments.add(new StaticSegment(segment.toString()));

		if (paramaterNameString != null && !"".equals(paramaterNameString)) {
			pathSegments.add(new ParameterSegment(required, allowSlashes, paramaterNameString, regex));
		}
	}

	public PathPatternBuilder apply(Map<String, String> parameters, Map<String, String> staticParameterValues) {
		PathPatternBuilder result = new PathPatternBuilder();

		for (PathSegment pathSegment : pathSegments) {
			result.append(pathSegment.apply(parameters, staticParameterValues));
		}

		return result;
	}

	/** Returns a new PathPatternBuilder with the pattern appended to it.
	 *  All parameters in the appended pattern are required.
	 */
	public PathPatternBuilder append(String pattern) {
		return append(pattern, null, null);
	}

	/** Returns a new PathPatternBuilder with the pattern appended to it.
	 *  The named parameters in the appended pattern are not required.
	 */
	public PathPatternBuilder append(String pattern, Set<String> optionalParameterNames, Map<String, String> parameterRegexes) {
		return append(PathPatternBuilder.parse(pattern, optionalParameterNames, parameterRegexes).createPathPattern());
	}

	/** Returns a new PathPatternBuilder with the pattern appended to it.
	 */
	public PathPatternBuilder append(PathPattern pathPattern) {
		PathPatternBuilder result = clone();

		Iterator<PathSegment> iterator = pathPattern.getPathSegments().iterator();

		if (iterator.hasNext()) {
			PathSegment current = iterator.next();

			result.append(current);

			while (iterator.hasNext()) {
				current = iterator.next();
				result.pathSegments.add(current.clone());
				if (current instanceof ParameterSegment) {
					result.parameterNames.add(((ParameterSegment) current).getName());
				}
			}
		}
		return result;
	}

	/** Returns a new PathPatternBuilder with the static path segment appended to it.
	 */
	public PathPatternBuilder appendStatic(String pathSegment) {
		PathSegment parameter = new StaticSegment(pathSegment);
		return appended(parameter);
	}

	/** Returns a new PathPatternBuilder with the parameter appended to it.
	 *  The parameter is required and does not allow slashes.
	 */
	public PathPatternBuilder appendParameter(String parameterName) {
		return appendParameter(parameterName, false);
	}

	/** Returns a new PathPatternBuilder with the parameter appended to it.
	 *  The parameter is required.
	 */
	public PathPatternBuilder appendParameter(String parameterName, boolean allowSlashes) {
		ParameterSegment parameter = new ParameterSegment(true, allowSlashes, parameterName);
		return appended(parameter);
	}

	/** Returns a new PathPatternBuilder with the UrlSegment appended.
	 */
	private PathPatternBuilder appended(PathSegment pathSegment) {
		PathPatternBuilder result = clone();
		result.append(pathSegment);
		return result;
	}

	/** Appends a UrlSegment.
	 */
	private void append(PathSegment pathSegment) {
		pathSegments.add(pathSegment.clone());
		if (pathSegment instanceof ParameterSegment) {
			ParameterSegment parameter = (ParameterSegment) pathSegment;
			parameterNames.add(parameter.getName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PathPatternBuilder clone() {
		try {
			PathPatternBuilder result = (PathPatternBuilder) super.clone();

			result.pathSegments = new ArrayList<PathSegment>(pathSegments.size());
			for (PathSegment parameter : pathSegments) {
				result.pathSegments.add(parameter.clone());
			}

			result.parameterNames = (HashSet<String>) parameterNames.clone();

			return result;
		}
		catch (CloneNotSupportedException ex) {
			throw new Error(ex);
		}
	}

	public PathPattern createPathPattern() {
		return new PathPattern(pathSegments, parameterNames);
	}

	/** Parses a String into a path pattern.
	 *
	 * Parameters whose name is contained in <code>optionalParameterNames</code>
	 * will not be required for paths to match.
	 * @param pattern describes the format of matching paths
	 * @param optionalParameterNames parameter names that are not required
	 * @param parameterRegexes the regular expressions to use for the parameter values
	 * @return a {@link PathPatternBuilder} that will match paths to the pattern
	 */
	public static PathPatternBuilder parse(String pattern, Set<String> optionalParameterNames, Map<String, String> parameterRegexes) {
		PathPatternBuilder builder = new PathPatternBuilder(optionalParameterNames, parameterRegexes);

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

		return builder;
	}

	/** Tests if the character is a valid name character: [A-Za-z0-9_]
	 */
	private static boolean isValidNameChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
	}
}
