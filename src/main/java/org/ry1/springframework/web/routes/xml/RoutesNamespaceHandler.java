/* $Id$ */

package org.ry1.springframework.web.routes.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class RoutesNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("routes", new RoutesBeanDefinitionParser());
		registerBeanDefinitionParser("with", new WithBeanDefinitionParser());
		registerBeanDefinitionParser("connect", new ConnectBeanDefinitionParser());
		registerBeanDefinitionParser("resource", new ResourceBeanDefinitionParser());
	}

}
