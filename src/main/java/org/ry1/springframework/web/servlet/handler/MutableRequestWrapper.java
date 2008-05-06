package org.ry1.springframework.web.servlet.handler;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/** Wraps a request, allowing extended parameters to be added.
 * @author rberdeen
 *
 */
public class MutableRequestWrapper extends HttpServletRequestWrapper {
	private String method;
	
	private Map<String, String[]> extendedParameters;
	private ShareableParameterMap sharableParameterMap;
	
	public MutableRequestWrapper(HttpServletRequest request) {
		super(request);
		this.method = request.getMethod();
	}
	
	/** Returns the extended parameters.
	 * @return
	 */
	public Map<String, String[]> getExtendedParameters() {
		return extendedParameters;
	}
	
	public void setExtendedParameters(Map<String, String[]> extendedParameters) {
		// so we can just check if extendedParemeters is null later on
		//if (extendedParameters != null && extendedParameters.size() > 0) {
			this.extendedParameters = extendedParameters;
		//}
	}

	@Override
	public String getParameter(String name) {
		if (extendedParameters != null) {
			String[] values = extendedParameters.get(name);
			if (values != null && values.length > 0) {
				return values[0];
			}
			else {
				return null;
			}
		}
		else {
			return super.getParameter(name);
		}
	}
	
	/** Returns the parameter from the wrapped request.
	 */
	public String getBaseParameter(String name) {
		return super.getParameter(name);
	}

	@Override
	public Map getParameterMap() {
		if (extendedParameters != null) {
			if (sharableParameterMap == null) {
				sharableParameterMap = new ShareableParameterMap();
			}
			return sharableParameterMap;
		}
		else {
			return super.getParameterMap();
		}
	}
	
	/** Returns the parameter map from the wrapped request.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String[]> getBaseParameterMap() {
		return super.getParameterMap();
	}

	@Override
	public Enumeration getParameterNames() {
		if (extendedParameters != null) {
			return Collections.enumeration(extendedParameters.keySet());
		}
		else {
			return super.getParameterNames();
		}
	}
	
	/** Returns the parameterNames from the wrapped request.
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<String> getBaseParameterNames() {
		return super.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		if (extendedParameters != null) {
			String[] result = extendedParameters.get(name);
			return result != null ? result.clone() : null;
		}
		else {
			return super.getParameterValues(name);
		}
	}
	
	/** Returns the parameter values from the wrapped request.
	 */
	public String[] getBaseParameterValues(String name) {
		return super.getParameterValues(name);
	}
	
	private class ShareableParameterMap extends AbstractMap<String, String[]> {
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsKey(Object key) {
			return extendedParameters.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return extendedParameters.containsValue(value);
		}

		@Override
		public String[] get(Object key) {
			return extendedParameters.get(key).clone();
		}

		@Override
		public Set<String> keySet() {
			return extendedParameters.keySet();
		}

		@Override
		public void putAll(Map<? extends String, ? extends String[]> t) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String[] remove(Object key) {
			throw new UnsupportedOperationException();
		}

		private EntrySet entrySet = new EntrySet();
		@Override
		public EntrySet entrySet() {
			return entrySet;
		}
		
		private class EntrySet extends AbstractSet<Map.Entry<String, String[]>> {
			EntrySetIterator entrySetIterator;
			
			@Override
			public Iterator<Entry<String, String[]>> iterator() {
				if (entrySetIterator == null) {
					entrySetIterator = new EntrySetIterator();
				}
				return entrySetIterator;
			}

			@Override
			public int size() {
				return extendedParameters.size();
			}
			
			@SuppressWarnings("unchecked")
			private class EntrySetIterator implements Iterator<Entry<String, String[]>> {
				private Iterator<Map.Entry<String, String[]>> iterator = MutableRequestWrapper.super.getParameterMap().entrySet().iterator();
				
				public boolean hasNext() {
					return iterator.hasNext();
				}

				public EntrySetIteratorEntry next() {
					return new EntrySetIteratorEntry(iterator.next());
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
				
				private class EntrySetIteratorEntry implements Map.Entry<String, String[]> {
					private Map.Entry<String, String[]> entry;
					
					private EntrySetIteratorEntry(Map.Entry<String, String[]> entry) {
						this.entry = entry;
					}
					
					public String getKey() {
						return entry.getKey();
					}

					public String[] getValue() {
						return entry.getValue().clone();
					}

					public String[] setValue(String[] value) {
						throw new UnsupportedOperationException();
					}
				}
			}
		}
	}
	
	/** Returns the MutableRequestWrapper for the request.
	 * Useful because the wrapper could itself be wrapped.
	 * @param request
	 * @return
	 */
	public static MutableRequestWrapper getParameterizableRequestWrapper(HttpServletRequest request) {
		if (request instanceof MutableRequestWrapper) {
			return (MutableRequestWrapper) request;
		}
		else if (request instanceof HttpServletRequestWrapper) {
			return getParameterizableRequestWrapper((HttpServletRequest) ((HttpServletRequestWrapper) request).getRequest());
		}
		else {
			return null;
		}
	}
	
	@Override
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
}
