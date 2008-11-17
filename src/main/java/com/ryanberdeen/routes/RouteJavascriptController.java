package com.ryanberdeen.routes;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;

public class RouteJavascriptController implements Controller, LastModified {
	private Mapping mapping;
	private Resource headerResource = new ClassPathResource("header.js", RouteJavascriptController.class);
	private long lastModified;
	
	public RouteJavascriptController(Mapping mapping) {
		this.mapping = mapping;
		lastModified = System.currentTimeMillis();
	}
	
	public ModelAndView handleRequest(HttpServletRequest request,  HttpServletResponse response) throws Exception {
		InputStreamReader in = new InputStreamReader(headerResource.getInputStream());
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
		
		return null;
	}
	
	public long getLastModified(HttpServletRequest request) {
		return lastModified;
	}
}
