package org.ry1.springframework.web.routes.xml;

/* $Id$ */

import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ResourceBeanDefinitionParser extends AbstractRouteListParser {
	private static final String GET_METHOD = "GET";
	private static final String POST_METHOD = "POST";
	private static final String PUT_METHOD = "PUT";
	private static final String DELETE_METHOD = "DELETE";
	
	private static final String SHOW_ACTION = "show";
	private static final String CREATE_ACTION = "create";
	private static final String UPDATE_ACTION = "update";
	private static final String DESTROY_ACTION = "destroy";
	
	private static void applyParameters(RouteParameters routeParameters, String method, String parameterName, String action) {
		routeParameters.routeParameters.put(parameterName, action);
		routeParameters.metaParameters.put("methods", method);
	}
	
	public void parseRouteList(ParserContext parserContext, Element element, ManagedList list, RouteParameters routeParameters) {
		routeParameters = new WithBeanDefinitionParser().applyRouteParameters(element, parserContext, routeParameters);
		
		String actionParamterName = "action";
		
		if (routeParameters.metaParameters.get("pattern") == null) {
			String idParameterName = routeParameters.getMetaParameter("idParameter", "id");
			routeParameters.metaParameters.put("pattern", ':' + idParameterName);
		}
		
		ConnectBeanDefinitionParser connectParser = new ConnectBeanDefinitionParser();
		
		applyParameters(routeParameters, GET_METHOD, actionParamterName, SHOW_ACTION);
		list.add(connectParser.parse(element, parserContext, routeParameters));
		
		applyParameters(routeParameters, POST_METHOD, actionParamterName, CREATE_ACTION);
		list.add(connectParser.parse(element, parserContext, routeParameters));
		
		applyParameters(routeParameters, PUT_METHOD, actionParamterName, UPDATE_ACTION);
		list.add(connectParser.parse(element, parserContext, routeParameters));
		
		applyParameters(routeParameters, DELETE_METHOD, actionParamterName, DESTROY_ACTION);
		list.add(connectParser.parse(element, parserContext, routeParameters));
	}

}
