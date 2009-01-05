package com.ryanberdeen.routes.path;

import java.util.Map;
import java.util.regex.Matcher;


public class StaticSegment implements PathSegment, Cloneable {
	private String value;
	private boolean required;

	public StaticSegment(String value, boolean required) {
		this.value = value;
		this.required = required;
	}

	public void appendRegex(StringBuilder regexBuilder) {
		if (!required) {
			regexBuilder.append("(?:");
		}
		PathPattern.appendStringToRegex(value, regexBuilder);
		if (!required) {
			regexBuilder.append("|$)");
		}
	}

	public void appendPath(PathBuilder pathBuilder, Map<String, Object> parameters, Map<String, String> staticParameterValues, Map<String, String> contextParameters) {
		pathBuilder.append(value, required);
	}

	public void appendTemplate(StringBuilder templateBuilder) {
		templateBuilder.append(value);
	}

	public int consumeMatch(Matcher matcher, int group, Map<String, String> parameters) {
		return 0;
	}
}