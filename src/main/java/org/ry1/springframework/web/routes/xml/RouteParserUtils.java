package org.ry1.springframework.web.routes.xml;

import java.util.HashSet;

import org.ry1.springframework.web.routes.DefaultRoute;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RouteParserUtils {
	
	public static DefaultRoute createRoute(RouteParameters routeParameters) {
		return new DefaultRoute(getPattern(routeParameters), routeParameters.routeParameters, getName(routeParameters), getMethods(routeParameters), getExcludedMethods(routeParameters));
	}
	
	/** Parses the element, using only parameters from the routeParameters argument.
	 */
	public static BeanDefinition createRouteBeanDefinition(Element element, ParserContext parserContext, RouteParameters routeParameters) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DefaultRoute.class);
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

		builder.addConstructorArg(getPattern(routeParameters));
		builder.addConstructorArg(routeParameters.routeParameters);
		builder.addConstructorArg(getName(routeParameters));
		builder.addConstructorArg(getMethods(routeParameters));
		builder.addConstructorArg(getExcludedMethods(routeParameters));
		
		return builder.getBeanDefinition();
	}
	
	public static BeanDefinition createAppliedRouteBeanDefinition(Element element, ParserContext parserContext, DefaultRoute route, RouteParameters routeParameters) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AppliedRouteFactoryBean.class);

		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		
		builder.addPropertyValue("route", route);
		builder.addPropertyValue("parameters", routeParameters.routeParameters);
		builder.addPropertyValue("methods", RouteParserUtils.getMethods(routeParameters));
		builder.addPropertyValue("excludedMethods", RouteParserUtils.getExcludedMethods(routeParameters));
		
		return builder.getBeanDefinition();
	}
	
	public static HashSet<String> getMethods(RouteParameters routeParameters) {
		return getMethods(routeParameters.getMetaParameter("methods"));
	}
	
	public static HashSet<String> getExcludedMethods(RouteParameters routeParameters) {
		return getMethods(routeParameters.getMetaParameter("excludedMethods"));
	}
	
	private static HashSet<String> getMethods(String value) {
		if (value == null || value.equals("any")) {
			return null;
		}
		String[] methodsArray = value.split(",");
		HashSet<String> methods = new HashSet<String>(methodsArray.length);
		for (String method : methodsArray) {
			methods.add(method.toUpperCase());
		}
		
		return methods;
	}
	
	private static String getName(RouteParameters routeParameters) {
		String name = routeParameters.metaParameters.get("name");
		if (name != null) {
			name = routeParameters.getMetaParameter("namePrefix", "") + name;
		}
		
		return name;
	}
	
	private static String getPattern(RouteParameters routeParameters) {
		String patternPrefix = routeParameters.getMetaParameter("patternPrefix", "");
		String pattern = routeParameters.getMetaParameter("pattern");
		return patternPrefix + pattern;
	}
	
	public static RouteParameters applyRouteParameters(Element element, RouteParameters routeParameters) {
		RouteParameters result = new RouteParameters(routeParameters);
		
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
