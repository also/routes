package org.ry1.springframework.web.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ry1.springframework.web.servlet.handler.MutableRequestWrapper;

public class ExtendedParameters {
	/** The strategy used to add parameters to a request.
	 */
	public enum Strategy {
		/** Use a request wrapper if available, or a request attribute otherwise. */
		AUTO,
		/** Use a request wrapper. */
		WRAPPER,
		/** Use a request attribute. */
		ATTRIBUTE;
	}
	
	/** The name of the attribute used to add parameters using a request attribute. */
	public static final String ATTRIBUTE_NAME = "org.ry1.springframework.web.util.ExtendedParameters.extendedParameters";
	
	/** Adds the extended parameters to the request using the specified strategy.
	 */
	public static final void addExtendedParameter(HttpServletRequest request, Strategy strategy, Map<String, ? extends Object> parameters) {
		if (parameters.size() > 0) {
			Map<String, String[]> extendedParameters = getExtendedParameters(request, strategy, true);
			
			String name;
			String[] values;
			String[] currentValues;
			for(Map.Entry<String, ? extends Object> parameterValueEntry : parameters.entrySet()) {
				name = parameterValueEntry.getKey();
				Object valueObject = parameterValueEntry.getValue();
				if (valueObject instanceof String[]) {
					values = (String[]) valueObject;
				}
				else  if (valueObject instanceof String) {
					values = new String[] {(String) valueObject};
				}
				else {
					throw new IllegalArgumentException("Invalid value type [" + valueObject.getClass() + "], only String and String[] allowed");
				}
				currentValues = extendedParameters.get(name);
				
				if (currentValues == null) {
					currentValues = values;
				}
				else {
					String[] newValues = new String[values.length + currentValues.length];
					System.arraycopy(values, 0, newValues, 0, values.length);
					System.arraycopy(currentValues, 0, newValues, values.length, currentValues.length);
					currentValues = newValues;
				}
				extendedParameters.put(name, currentValues);
			}
		}
	}
	
	/** Retrieves the extended parameters from the request using the specified strategy.
	 */
	public static final Map<String, String[]> getExtendedParameters(HttpServletRequest request, Strategy strategy, boolean create) {
		Map<String, String[]> extendedParameters = null;
		if (strategy != Strategy.ATTRIBUTE) {
			extendedParameters = getExtendedParametersFromWrapper(request, create);
			if (extendedParameters == null) {
				if (strategy == Strategy.AUTO) {
					extendedParameters = getExtendedParametersFromAttribute(request, create);
				}
				else if (create) {
					throw new RuntimeException("WRAPPER strategy requires request wrapper.");
				}
			}
		}
		else {
			extendedParameters = getExtendedParametersFromAttribute(request, create);
		}
		
		return extendedParameters;
	}
	
	@SuppressWarnings("unchecked")
	private static final Map<String, String[]> getExtendedParametersFromWrapper(HttpServletRequest request, boolean create) {
		Map<String, String[]> extendedParameters = null;
		MutableRequestWrapper wrapper = MutableRequestWrapper.getParameterizableRequestWrapper(request);
		if (wrapper != null) {
			extendedParameters = wrapper.getExtendedParameters();
			if (extendedParameters == null && create) {
				extendedParameters = new HashMap<String, String[]>();
				extendedParameters.putAll(request.getParameterMap());
				wrapper.setExtendedParameters(extendedParameters);
			}
			return extendedParameters;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static final Map<String, String[]> getExtendedParametersFromAttribute(HttpServletRequest request, boolean create) {
		Map<String, String[]> extendedParameters = (Map<String, String[]>) request.getAttribute(ATTRIBUTE_NAME);
		if (extendedParameters == null && create) {
			extendedParameters = new HashMap<String, String[]>();
			extendedParameters.putAll(request.getParameterMap());
			request.setAttribute(ATTRIBUTE_NAME, extendedParameters);
		}
		return extendedParameters;
	}
}
