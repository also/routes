package com.ryanberdeen.routes.tags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

public class AnchorTag extends AbstractUrlTag {
	private HashMap<String, String> attributes = new HashMap<String, String>();
	
	@Override
	public void doTag() throws JspException, IOException {
		JspWriter out = getJspContext().getOut();
		out.write("<a href=\"" + getUrl() + "\"");
		
		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			out.write(' ');
			out.write(attribute.getKey());
			out.write("=\"");
			// TODO encode?
			out.write(attribute.getValue());
			out.write('"');
		}
		
		out.write('>');
		getJspBody().invoke(out);
		out.write("</a>");
	}
	
	@Override
	protected void setAttribute(String uri, String localName, Object value) {
		if (localName.startsWith("tag")) {
			attributes.put(localName.substring(3), value.toString());
		}
	}
}
