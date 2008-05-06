/* $Id$ */

package org.ry1.springframework.web.routes.xml;

import java.util.HashMap;

import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class WithBeanDefinitionParser extends AbstractRouteListParser {
	
	public void parseRouteList(ParserContext parserContext, Element element, ManagedList list, RouteParameters routeParameters) {
		routeParameters = applyRouteParameters(element, parserContext, routeParameters);
		super.parseRouteList(parserContext, element, list, routeParameters);
	}
	
	protected RouteParameters applyRouteParameters(Element element, ParserContext parserContext, RouteParameters routeParameters) {
		RouteParameters result = new RouteParameters(new HashMap<String, String>(), new HashMap<String, String>());
		if (routeParameters != null) {
			result.metaParameters.putAll(routeParameters.metaParameters);
			result.routeParameters.putAll(routeParameters.routeParameters);
		}
		
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); ++i) {
			Node node = attributes.item(i);
			String value = node.getTextContent();
			String parameterName = node.getLocalName();
			if (node.getNamespaceURI() != null) {
				result.metaParameters.put(parameterName, value);
			}
			else {
				result.routeParameters.put(parameterName, value);
			}
		}
		
		return result;
	}
}
