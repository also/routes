package com.ryanberdeen.routes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.ryanberdeen.routes.builder.PathPatternBuilder;

public class UrlPatternTest {
	private PathPatternBuilder noParameters;
	private PathPatternBuilder simplePatternAfter;
	private PathPatternBuilder simplePatternBefore;
	private PathPatternBuilder simplePatternDefault;

	private static final Set<String> NO_PARAMETER_NAMES = Collections.emptySet();
	private static final Map<String, String> NO_PARAMETER_VALUES = Collections.emptyMap();

	@Before
	public void setUp() {
		noParameters = PathPatternBuilder.parse("noParameters");
		simplePatternAfter = PathPatternBuilder.parse("before/:parameter");
		simplePatternBefore = PathPatternBuilder.parse(":parameter/after");
		simplePatternDefault = PathPatternBuilder.parse("before/:parameter/");
	}

	@Test
	public void testAppendStatic() {
		PathPattern pattern;
		Map<String, String> match;

		pattern = noParameters.appendStatic("/appended").createPathPattern(NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		match = pattern.match("noParameters/appended");
		// the pattern should have matched
		assertNotNull(match);
		// with no parameters
		assertEquals(match.size(), 0);

		pattern = simplePatternAfter.appendStatic("/after").createPathPattern(NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		match = pattern.match("before/value/after");
		assertNotNull(match);
		assertEquals(match, Collections.singletonMap("parameter", "value"));
	}

	@Test
	public void testAppendParameter() {
		PathPattern pattern;
		Map<String, String> match;

		pattern = noParameters.appendParameter("parameter").createPathPattern(NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		match = pattern.match("noParametersvalue");
		assertNotNull(match);
		assertEquals(match, Collections.singletonMap("parameter", "value"));

		pattern = simplePatternAfter.appendStatic("/").appendParameter("parameter2").createPathPattern(NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		match = pattern.match("before/value/value2");
		assertNotNull(match);
	}

	@Test
	public void testApply() {
		PathPattern pattern;
		Map<String, String> match;

		pattern = simplePatternAfter.apply(Collections.singletonMap("parameter", "value"), NO_PARAMETER_VALUES).createPathPattern(NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		match = pattern.match("before/value");
		assertNotNull(match);
		assertEquals(0, match.size());

		pattern = simplePatternBefore.apply(Collections.singletonMap("parameter", "value"), NO_PARAMETER_VALUES).createPathPattern(NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		match = pattern.match("value/after");
		assertNotNull(match);
		assertEquals(0, match.size());
		match = pattern.match("/after");
		assertNull(match);

		Map<String, String> parameters = Collections.singletonMap("parameter", "value(");
		pattern = simplePatternDefault.apply(parameters, Collections.singletonMap("parameter", "value(")).createPathPattern(Collections.singleton("parameter"), NO_PARAMETER_VALUES);
		match = pattern.match("before/value(");
		assertNotNull(match);
		assertEquals(0, match.size());

		match = pattern.match("before");
		assertNotNull(match);
		assertEquals(0, match.size());
	}
}
