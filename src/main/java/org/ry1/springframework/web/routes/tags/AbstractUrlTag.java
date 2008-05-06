/* $Id$ */

package org.ry1.springframework.web.routes.tags;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.ry1.springframework.web.routes.RouteUtils;

public abstract class AbstractUrlTag extends SimpleTagSupport implements DynamicAttributes {
	private HashMap<String, Object> parameters = new HashMap<String, Object>();
	
	private String name;
	private boolean includeContextPath = true;
	
	@SuppressWarnings("unchecked")
	public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
		if (uri != null) {
			if (localName.equals("parameters")) {
				parameters.putAll((Map<String, Object>) value);
			}
			else if (localName.equals("name")) {
				name = value.toString();
			}
			else if (localName.equals("includeContextPath")) {
				includeContextPath = Boolean.parseBoolean(value.toString());
			}
			else {
				setAttribute(uri, localName, value);
			}
		}
		else {
			parameters.put(localName, String.valueOf(value));
		}
	}
	
	protected String getUrl() {
		PageContext pageContext = (PageContext) getJspContext();
		
		String url;
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		
		if (name != null) {
			url = RouteUtils.getMapping(request).getUrl(request, name, parameters, includeContextPath);
		}
		else {
			url = RouteUtils.getMapping(request).getUrl(request, parameters, includeContextPath);
		}
		
		return url;
	}
	
	protected void setAttribute(String uri, String localName, Object value) {
		
	}
	
}
