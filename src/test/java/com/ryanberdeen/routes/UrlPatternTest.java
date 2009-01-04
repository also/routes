package com.ryanberdeen.routes;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.ryanberdeen.routes.UrlPattern;
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
		noParameters = PathPatternBuilder.parse("noParameters", NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		simplePatternAfter = PathPatternBuilder.parse("before/:parameter", NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		simplePatternBefore = PathPatternBuilder.parse(":parameter/after", NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		simplePatternDefault = PathPatternBuilder.parse("before/:parameter/", Collections.singleton("parameter"), NO_PARAMETER_VALUES);
	}

	@Test
	public void testAppendStatic() {
		UrlPattern pattern;
		Map<String, String> match;

		pattern = noParameters.appendStatic("/appended").createPathPattern();
		match = pattern.match("noParameters/appended");
		// the pattern should have matched
		assertNotNull(match);
		// with no parameters
		assertEquals(match.size(), 0);

		pattern = simplePatternAfter.appendStatic("/after").createPathPattern();
		match = pattern.match("before/value/after");
		assertNotNull(match);
		assertEquals(match, Collections.singletonMap("parameter", "value"));
	}

	@Test
	public void testAppendParameter() {
		UrlPattern pattern;
		Map<String, String> match;

		pattern = noParameters.appendParameter("parameter").createPathPattern();
		match = pattern.match("noParametersvalue");
		assertNotNull(match);
		assertEquals(match, Collections.singletonMap("parameter", "value"));

		pattern = simplePatternAfter.appendStatic("/").appendParameter("parameter2").createPathPattern();
		match = pattern.match("before/value/value2");
		assertNotNull(match);
	}

	@Test
	public void testApply() {
		UrlPattern pattern;
		Map<String, String> match;

		pattern = simplePatternAfter.apply(Collections.singletonMap("parameter", "value"), NO_PARAMETER_VALUES).createPathPattern();
		match = pattern.match("before/value");
		assertNotNull(match);
		assertEquals(0, match.size());

		pattern = simplePatternBefore.apply(Collections.singletonMap("parameter", "value"), NO_PARAMETER_VALUES).createPathPattern();
		match = pattern.match("value/after");
		assertNotNull(match);
		assertEquals(0, match.size());
		match = pattern.match("/after");
		assertNull(match);

		Map<String, String> parameters = Collections.singletonMap("parameter", "value(");
		pattern = simplePatternDefault.apply(parameters, Collections.singletonMap("parameter", "value(")).createPathPattern();
		match = pattern.match("before/value(");
		assertNotNull(match);
		assertEquals(0, match.size());

		match = pattern.match("before");
		assertNotNull(match);
		assertEquals(0, match.size());
	}
}
