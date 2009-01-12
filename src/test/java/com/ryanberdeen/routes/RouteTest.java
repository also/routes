package com.ryanberdeen.routes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ryanberdeen.routes.Route;
import com.ryanberdeen.routes.builder.RouteBuilderUtils;

import static org.junit.Assert.*;

public class RouteTest {
	private Route simple;

	@Before
	public void setUp() {
		HashMap<String, String> simpleParameters = new HashMap<String, String>();
		simpleParameters.put("controller", "instructorLecture");
		simpleParameters.put("action", "show");
		simple = RouteBuilderUtils.buildRoute("/instructor/lectures/:id/:action", simpleParameters);
		simple.prepare();
	}

	/*@Test
	public void testApply() {
		Route route = simple.apply(Collections.singletonMap("id", "1"), null, null);
		Map<String, String> match = route.match("/instructor/lectures/1", null);
		assertNotNull("Valid URL did not match", match);
		assertEquals("Incorrect number of parameters", 3, match.size());
		assertEquals("Invalid value for parameter", "show", match.get("action"));
		assertEquals("Invalid value for parameter", "1", match.get("id"));

		// only id 1 should match
		match = route.match("/instructor/lectures/2", null);
		assertNull("Invalid URL matched", match);

		// id 1 should be required
		match = route.match("/instructor/lectures/", null);
		assertNull("Invalid URL matched", match);

		match = route.match("/instructor/lectures", null);
		assertNull("Invalid URL matched", match);
	}*/

	@Test
	public void testMatchUrlIncludeOptional() {
		Map<String, String> match = simple.match("/instructor/lectures/1/edit", null);
		assertNotNull("Valid URL did not match", match);
		assertEquals(3, match.size());
		assertEquals("edit", match.get("action"));
		assertEquals("1", match.get("id"));
	}

	@Test
	public void testMatchUrlExcludeTrailingOptional() {
		Map<String, String> match = simple.match("/instructor/lectures/1/", null);
		assertNotNull("URL did not match", match);
		assertEquals(3, match.size());
		assertEquals("show", match.get("action"));
		assertEquals("1", match.get("id"));

		// the trailing slash should not be required
		match = simple.match("/instructor/lectures/1", null);
		assertNotNull("URL did not match", match);
		assertEquals(3, match.size());
		assertEquals("show", match.get("action"));
		assertEquals("1", match.get("id"));
	}

	public void testMatchParameters() {

	}

	@Test
	public void testDefaultStaticParameters() {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("controller", "lecture");
		parameters.put("action", "index");
		Route route = RouteBuilderUtils.buildRoute("/lecture", parameters);
		route.setDefaultStaticParameters(Collections.singletonMap("action", "index"));
		route.prepare();
		int match = route.match(Collections.singletonMap("controller", (Object) "lecture"), Route.NO_PARAMETER_VALUES);
		assertEquals(1, match);
	}
}
