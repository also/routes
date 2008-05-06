/* $Id$ */

package org.ry1.springframework.web.routes.xml;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RoutesBeanDefinitionParser extends AbstractRouteListParser {
	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
		if (element.hasAttribute(ID_ATTRIBUTE)) {
			return element.getAttribute(ID_ATTRIBUTE);
		}
		else {
			return parserContext.getReaderContext().generateBeanName(definition);
		}
	}
}
