package com.ryanberdeen.routes;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.ryanberdeen.routes.UrlPattern;

public class UrlPatternTest {
	private UrlPattern noParameters;
	private UrlPattern simplePatternAfter;
	private UrlPattern simplePatternBefore;
	private UrlPattern simplePatternDefault;

	private static final Set<String> NO_PARAMETER_NAMES = Collections.emptySet();
	private static final Map<String, String> NO_PARAMETER_VALUES = Collections.emptyMap();

	@Before
	public void setUp() {
		noParameters = UrlPattern.parse("noParameters", NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		simplePatternAfter = UrlPattern.parse("before/:parameter", NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		simplePatternBefore = UrlPattern.parse(":parameter/after", NO_PARAMETER_NAMES, NO_PARAMETER_VALUES);
		simplePatternDefault = UrlPattern.parse("before/:parameter/", Collections.singleton("parameter"), NO_PARAMETER_VALUES);
	}

	@Test
	public void testAppendStatic() {
		UrlPattern pattern;
		Map<String, String> match;

		pattern = noParameters.appendStatic("/appended");
		match = pattern.match("noParameters/appended");
		// the pattern should have matched
		assertNotNull(match);
		// with no parameters
		assertEquals(match.size(), 0);

		pattern = simplePatternAfter.appendStatic("/after");
		match = pattern.match("before/value/after");
		assertNotNull(match);
		assertEquals(match, Collections.singletonMap("parameter", "value"));
	}

	@Test
	public void testAppendParameter() {
		UrlPattern pattern;
		Map<String, String> match;

		pattern = noParameters.appendParameter("parameter");
		match = pattern.match("noParametersvalue");
		assertNotNull(match);
		assertEquals(match, Collections.singletonMap("parameter", "value"));

		pattern = simplePatternAfter.appendStatic("/").appendParameter("parameter2");
		match = pattern.match("before/value/value2");
		assertNotNull(match);
	}

	@Test
	public void testApply() {
		UrlPattern pattern;
		Map<String, String> match;

		pattern = simplePatternAfter.apply(Collections.singletonMap("parameter", "value"), NO_PARAMETER_VALUES);
		match = pattern.match("before/value");
		assertNotNull(match);
		assertEquals(0, match.size());

		pattern = simplePatternBefore.apply(Collections.singletonMap("parameter", "value"), NO_PARAMETER_VALUES);
		match = pattern.match("value/after");
		assertNotNull(match);
		assertEquals(0, match.size());
		match = pattern.match("/after");
		assertNull(match);

		Map<String, String> parameters = Collections.singletonMap("parameter", "value(");
		pattern = simplePatternDefault.apply(parameters, Collections.singletonMap("parameter", "value("));
		match = pattern.match("before/value(");
		assertNotNull(match);
		assertEquals(0, match.size());

		match = pattern.match("before");
		assertNotNull(match);
		assertEquals(0, match.size());
	}
}
