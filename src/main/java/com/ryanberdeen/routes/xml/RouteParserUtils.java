package com.ryanberdeen.routes.xml;

import java.util.HashMap;
import java.util.HashSet;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ryanberdeen.routes.Route;
import com.ryanberdeen.routes.UrlPattern;

public class RouteParserUtils {
	
	public static Route createRoute(RouteParameters routeParameters) {
		Route result = new Route(getPattern(routeParameters), routeParameters.parameterValues, routeParameters.parameterRegexes);
		result.setDefaultStaticParameters(routeParameters.defaultStaticParameterValues);
		result.setName(getName(routeParameters));
		result.setMethods(getMethods(routeParameters));
		result.setExcludedMethods(getExcludedMethods(routeParameters));
		
		return result;
	}
	
	public static UrlPattern createUrlPattern(RouteParameters routeParameters) {
		return UrlPattern.parse(getPattern(routeParameters), routeParameters.parameterValues.keySet(), routeParameters.parameterRegexes);
	}
	
	/** Parses the element, using only parameters from the parameterValues argument.
	 */
	public static BeanDefinition createRouteBeanDefinition(Element element, ParserContext parserContext, RouteParameters routeParameters) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Route.class);
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

		builder.addConstructorArgValue(getPattern(routeParameters));
		builder.addConstructorArgValue(routeParameters.parameterValues);
		builder.addConstructorArgValue(routeParameters.parameterRegexes);
		addPropertyValues(builder, routeParameters);
		
		return builder.getBeanDefinition();
	}
	
	public static BeanDefinition createAppliedRouteBeanDefinition(Element element, ParserContext parserContext, UrlPattern pattern, RouteParameters baseParameters, RouteParameters applyParameters) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Route.class);

		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		
		HashMap<String, String> routeParameters = (HashMap<String, String>) baseParameters.parameterValues.clone();
		routeParameters.putAll(applyParameters.parameterValues);
		
		UrlPattern appliedPattern = pattern.apply(applyParameters.parameterValues, baseParameters.parameterValues);
		
		builder.addPropertyValue("urlPattern", appliedPattern);
		builder.addPropertyValue("staticParameters", routeParameters);
		addPropertyValues(builder, baseParameters);
		
		return builder.getBeanDefinition();
	}
	
	public static BeanDefinition createRouteBeanDefinition(Element element, ParserContext parserContext, UrlPattern pattern, RouteParameters routeParameters) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Route.class);

		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));

		builder.addPropertyValue("urlPattern", pattern);
		builder.addPropertyValue("staticParameters", routeParameters.parameterValues);
		addPropertyValues(builder, routeParameters);
		
		return builder.getBeanDefinition();
	}
	
	private static void addPropertyValues(BeanDefinitionBuilder builder, RouteParameters routeParameters) {
		builder.addPropertyValue("defaultStaticParameters", routeParameters.defaultStaticParameterValues);
		builder.addPropertyValue("name", getName(routeParameters));
		builder.addPropertyValue("methods", getMethods(routeParameters));
		builder.addPropertyValue("excludedMethods", getExcludedMethods(routeParameters));
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
				result.parameterValues.put(parameterName, value);
			}
		}
		
		applyRouteParameterTags(element, result);
		
		return result;
	}
	
	public static void applyRouteParameterTags(Element element, RouteParameters routeParameters) {
		NodeList children = element.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element child =  (Element) node;
				if (child.getTagName().equals("parameter")) {
					applyParameter(child, routeParameters);
				}
			}
		}
	}

	private static void applyParameter(Element element, RouteParameters routeParameters) {
		String name = element.getAttribute("name");
		if (element.hasAttribute("value")) {
			String value = element.getAttribute("value");
			routeParameters.parameterValues.put(name, value);
			if (element.hasAttribute("default")) {
				boolean defaultStaticParameter = Boolean.valueOf(element.getAttribute("default"));
				if (defaultStaticParameter) {
					routeParameters.defaultStaticParameterValues.put(name, value);
				}
			}
		}
		if (element.hasAttribute("pattern")) {
			routeParameters.parameterRegexes.put(name, element.getAttribute("pattern"));
		}
	}
}
