package org.ry1.springframework.web.servlet.handler;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/** Replaces requests with <code>MutableRequestWrapper</code> to allow for extended parameters.
 * @see org.ry1.springframework.web.util.ExtendedParameters.Strategy.WRAPPER
 * @author rberdeen
 */
public class MutableRequestFilter implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException {}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new MutableRequestWrapper((HttpServletRequest) request), response);
	}

	public void destroy() {}
}
