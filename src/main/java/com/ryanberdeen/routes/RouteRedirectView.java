package com.ryanberdeen.routes;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

public class RouteRedirectView implements View {
	private HashMap<String, Object> parameters = new HashMap<String, Object>();
	
	public RouteRedirectView(Object... parameterArray) {
		if (parameterArray.length % 2 != 0) {
			throw new IllegalArgumentException("Parameters must have an even number of elements");
		}
		
		for (int i = 0; i < parameterArray.length; i += 2) {
			parameters.put((String) parameterArray[i], parameterArray[i + 1]);
		}
	}
	
	public String getContentType() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String url = RouteUtils.getMapping(request).getUrl(request, parameters, true);
		
		response.setStatus(303);
		response.setHeader("Location", response.encodeRedirectURL(url));
	}

}
