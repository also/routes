package org.ry1.springframework.web.servlet.handler;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/** Replaces requests with <code>MutableRequestWrapper</code> to allow for extended parameters.
 * @see org.ry1.springframework.web.util.ExtendedParameters.Strategy.WRAPPER
 * @author rberdeen
 */
public class MutableRequestFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new MutableRequestWrapper(request), response);
	}
}
