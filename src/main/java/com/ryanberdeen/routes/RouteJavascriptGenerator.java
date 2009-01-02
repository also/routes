package com.ryanberdeen.routes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generates JavaScript to support routes. The result is written to the
 * response output stream.
 *
 * <p>The route JavaScript allows named routes to be used in JavaScript.</p>
 */
public class RouteJavascriptGenerator {
	private HttpServletRequestMapping mapping;

	/**
	 * Sets the mapping for which JavaScript will be generated.
	 */
	public void setMapping(HttpServletRequestMapping mapping) {
		this.mapping = mapping;
	}

	/**
	 * Generates and writes the route JavaScript.
	 */
	public void generate(HttpServletRequest request,  HttpServletResponse response) throws IOException {
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("header.js"));
		PrintWriter writer = response.getWriter();
		char[] buffer = new char[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			writer.write(buffer, 0, len);
		}

		writer.write('{');
		boolean first = true;
		for (Map.Entry<String, Route> entry : mapping.getNamedRoutes().entrySet()) {
			if (!first) {
				writer.write(',');
			}
			else {
				first = false;
			}
			writer.write('\'');
			writer.write(entry.getKey());
			writer.write("':'");
			writer.write(request.getContextPath());
			writer.write(entry.getValue().getUrlPattern().getStringTemplate());
			writer.write('\'');

		}
		writer.write("};");
	}
}
