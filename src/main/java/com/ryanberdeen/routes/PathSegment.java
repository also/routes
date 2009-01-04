package com.ryanberdeen.routes;

import java.util.Map;


public interface PathSegment {
	/** Appends the regex that represents this segment to the builder.
	 */
	public void appendRegex(StringBuilder regexBuilder);

	/** Appends the path generated by applying the parameters to this segment to the builder.
	 */
	public void appendPath(PathBuilder pathBuilder, Map<String, Object> parameters, Map<String, String> staticParameters, Map<String, String> contextParameters);

	/** Appends the template that represents this segment to the builder.
	 */
	public void appendTemplate(StringBuilder templateBuilder);
}