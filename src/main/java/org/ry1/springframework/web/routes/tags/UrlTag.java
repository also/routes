/* $Id$ */

package org.ry1.springframework.web.routes.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

public class UrlTag extends AbstractUrlTag {
	private String var;
	private String scope = "page";

	@Override
	public void doTag() throws JspException, IOException {
		PageContext pageContext = (PageContext) getJspContext();
		String url = getUrl();
		
		if (var != null) {
			int varScope = PageContext.PAGE_SCOPE;
			if ("request".equals(scope)) {
				varScope = PageContext.REQUEST_SCOPE;
			}
			pageContext.setAttribute(var, url, varScope);
		}
		else {
			JspWriter out = getJspContext().getOut();
			out.print(url);
		}
	}
	
	@Override
	protected void setAttribute(String uri, String localName, Object value) {
		if (uri != null) {
			if (localName.equals("var")) {
				var = value.toString();
			}
			else if (localName.equals("scope")) {
				scope = value.toString();
			}
		}
	}
}
