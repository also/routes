/* $Id$ */

package com.ryanberdeen.routes.xml;

import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class WithBeanDefinitionParser extends AbstractRouteListParser {
	public void parseRouteList(ParserContext parserContext, Element element, ManagedList list, RouteParameters routeParameters) {
		routeParameters = RouteParserUtils.applyRouteParameters(element, routeParameters);
		super.parseRouteList(parserContext, element, list, routeParameters);
	}
}
