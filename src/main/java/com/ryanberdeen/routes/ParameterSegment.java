package com.ryanberdeen.routes;

import java.util.Map;


public class ParameterSegment implements PathSegment, Cloneable {
	static final String VALUE_WITHOUT_SLASHES = "[^/]+";
	static final String VALUE_WITH_SLASHES = ".+";

	private boolean required;
	private boolean allowSlashes;
	private String name;
	private String regex;

	public ParameterSegment(boolean required, boolean allowSlashes, String name) {
		this(required, allowSlashes, name, null);
	}

	public ParameterSegment(boolean required, boolean allowSlashes, String name, String regex) {
		this.required = required;
		this.allowSlashes = allowSlashes;
		this.name = name;
		this.regex = regex;
	}

	public String getName() {
		return name;
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

	public void appendPath(PathBuilder pathBuilder, Map<String, Object> parameters, Map<String, String> staticParameterValues, Map<String, String> contextParameters) {
		Object result = parameters.get(name);
		if (result == null) {
			result = staticParameterValues.get(name);
			if (result == null) {
				String contextValue = contextParameters.get(name);
				if (contextValue != null) {
					result = contextValue;
				}
			}
		}

		if (result == null) {
			throw new RuntimeException("No value for [" + name + "]");
		}
		pathBuilder.append(result, required || !result.equals(staticParameterValues.get(name)));
	}

	public void appendTemplate(StringBuilder templateBuilder) {
		templateBuilder.append("${");
		templateBuilder.append(name);
		templateBuilder.append("}");
	}
}