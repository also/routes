/* $Id$ */

package org.ry1.springframework.web.routes.xml;

import java.util.HashSet;

import org.ry1.springframework.web.routes.DefaultRoute;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ConnectBeanDefinitionParser implements BeanDefinitionParser {
	
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return parse(element, parserContext, null);
	}
	
	public BeanDefinition parse(Element element, ParserContext parserContext, RouteParameters routeParameters) {
		routeParameters = new WithBeanDefinitionParser().applyRouteParameters(element, parserContext, routeParameters);
		
		return parseCustom(element, parserContext, routeParameters);
	}
	
	/** Parses the element, using only parameters from the routeParameters argument.
	 */
	public BeanDefinition parseCustom(Element element, ParserContext parserContext, RouteParameters routeParameters) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
		builder.getRawBeanDefinition().setBeanClass(DefaultRoute.class);
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		
		String patternPrefix = routeParameters.getMetaParameter("patternPrefix", "");
		String pattern = routeParameters.getMetaParameter("pattern", element.getTextContent());
		pattern = patternPrefix + pattern;
		
		String namePrefix = routeParameters.getMetaParameter("namePrefix", "");
		String name = routeParameters.metaParameters.get("name");
		name = namePrefix + name;
		
		HashSet<String> methods = getMethods(routeParameters.metaParameters.get("methods"));
		HashSet<String> excludedMethods = getMethods(routeParameters.metaParameters.get("excludedMethods"));

		builder.addConstructorArg(pattern);
		builder.addConstructorArg(routeParameters.routeParameters);
		builder.addConstructorArg(name);
		builder.addConstructorArg(methods);
		builder.addConstructorArg(excludedMethods);
		
		return builder.getBeanDefinition();
	}
	
	private HashSet<String> getMethods(String value) {
		if (value == null) {
			return null;
		}
		String[] methodsArray = value.split(",");
		HashSet<String> methods = new HashSet<String>(methodsArray.length);
		for (String method : methodsArray) {
			methods.add(method.toUpperCase());
		}
		
		return methods;
	}

}
