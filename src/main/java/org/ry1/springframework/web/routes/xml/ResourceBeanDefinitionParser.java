/* $Id$ */

package org.ry1.springframework.web.routes.xml;

import org.ry1.springframework.web.routes.DefaultRoute;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ResourceBeanDefinitionParser extends AbstractRouteListParser {
	private static final String GET_METHOD = "GET";
	private static final String POST_METHOD = "POST";
	private static final String PUT_METHOD = "PUT";
	private static final String DELETE_METHOD = "DELETE";
	
	private static final String SHOW_ACTION = "show";
	private static final String CREATE_ACTION = "create";
	private static final String UPDATE_ACTION = "update";
	private static final String DESTROY_ACTION = "destroy";
	
	private static RouteParameters applyParameters(RouteParameters routeParameters, String method, String parameterName, String action) {
		RouteParameters result = new RouteParameters(routeParameters);
		result.routeParameters.put(parameterName, action);
		result.metaParameters.put("methods", method);
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void parseRouteList(ParserContext parserContext, Element element, ManagedList list, RouteParameters routeParameters) {
		routeParameters = RouteParserUtils.applyRouteParameters(element, routeParameters);
		
		String pattern = routeParameters.getMetaParameter("pattern", "");
		String patternPrefix = routeParameters.getMetaParameter("patternPrefix", "");
		routeParameters.metaParameters.put("patternPrefix", patternPrefix + pattern);
		
		String actionParamterName = "action";
		
		NodeList children = element.getChildNodes();
		
		String collectionPattern = routeParameters.getMetaParameter("collectionPattern", ":action");
		RouteParameters collectionParameters = new RouteParameters(routeParameters);
		collectionParameters.routeParameters.clear();
		collectionParameters.metaParameters.put("pattern", collectionPattern);
		DefaultRoute collectionRoute = RouteParserUtils.createRoute(collectionParameters);
		
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element child =  (Element) node;
				
				if (child.getTagName().equals("collection")) {
					parseCollections(parserContext, child, list, collectionRoute, collectionParameters);
				}
			}
		}
		
		collectionParameters.routeParameters.put(actionParamterName, "index");
		list.add(RouteParserUtils.createAppliedRouteBeanDefinition(element, parserContext, collectionRoute, collectionParameters));
		
		String idParameterName = routeParameters.getMetaParameter("idParameter", "id");
		routeParameters.metaParameters.put("pattern", ':' + idParameterName + "/:" + actionParamterName);
		
		DefaultRoute resourceRoute = RouteParserUtils.createRoute(routeParameters);
		routeParameters.routeParameters.clear();
		RouteParameters appliedParameters;
		
		appliedParameters = applyParameters(routeParameters, GET_METHOD, actionParamterName, SHOW_ACTION);
		list.add(RouteParserUtils.createAppliedRouteBeanDefinition(element, parserContext, resourceRoute, appliedParameters));
		
		appliedParameters = applyParameters(routeParameters, POST_METHOD, actionParamterName, CREATE_ACTION);
		list.add(RouteParserUtils.createAppliedRouteBeanDefinition(element, parserContext, resourceRoute, appliedParameters));
		
		appliedParameters = applyParameters(routeParameters, PUT_METHOD, actionParamterName, UPDATE_ACTION);
		list.add(RouteParserUtils.createAppliedRouteBeanDefinition(element, parserContext, resourceRoute, appliedParameters));
		
		appliedParameters = applyParameters(routeParameters, DELETE_METHOD, actionParamterName, DESTROY_ACTION);
		list.add(RouteParserUtils.createAppliedRouteBeanDefinition(element, parserContext, resourceRoute, appliedParameters));
	}

	@SuppressWarnings("unchecked")
	private static void parseCollections(ParserContext parserContext, Element element, ManagedList list, DefaultRoute route, RouteParameters routeParameters) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element child =  (Element) node;
				RouteParameters appliedParameters = RouteParserUtils.applyRouteParameters(child, new RouteParameters(routeParameters));
				
				list.add(RouteParserUtils.createAppliedRouteBeanDefinition(element, parserContext, route, appliedParameters));
			}
		}
	}
}
