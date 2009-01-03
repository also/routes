package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ryanberdeen.routes.UrlPattern;
import com.ryanberdeen.routes.UrlPattern.ParameterSegment;
import com.ryanberdeen.routes.UrlPattern.StaticSegment;
import com.ryanberdeen.routes.UrlPattern.UrlSegment;

public class PathPatternBuilder {
	private static final char PARAMETER_WITHOUT_SLASHES_PREFIX = ':';
	private static final char PARAMETER_WITH_SLASHES_PREFIX = '*';

	private Set<String> optionalParameterNames;
	private Map<String, String> parameterRegexes;
	//private UrlPattern urlPattern;
	private HashSet<String> parameterNames;
	private ArrayList<UrlSegment> pathSegments;

	public PathPatternBuilder(Set<String> optionalParameterNames, Map<String, String> parameterRegexes) {
		this.optionalParameterNames = optionalParameterNames;
		this.parameterRegexes = parameterRegexes;
		pathSegments = new ArrayList<UrlSegment>();
		parameterNames = new HashSet<String>();
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

	public UrlPattern createPathPattern() {
		return new UrlPattern(pathSegments, parameterNames);
	}

	/** Parses a String into a URL pattern.
	 *
	 * Parameters whose name is contained in <code>optionalParameterNames</code>
	 * will not be required for URLs to match.
	 * @param pattern describes the format of matching URLs
	 * @param optionalParameterNames parameter names that are not required
	 * @param parameterRegexes the regular expressions to use for the parameter values
	 * @return a {@link UrlPattern} that will match URLs to the pattern
	 */
	public static UrlPattern parse(String pattern, Set<String> optionalParameterNames, Map<String, String> parameterRegexes) {
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

		return builder.createPathPattern();
	}

	/** Tests if the character is a valid name character: [A-Za-z0-9_]
	 */
	private static boolean isValidNameChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
	}
}
