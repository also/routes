package com.ryanberdeen.routes;

public class PathGenerationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PathGenerationException() {}

	public PathGenerationException(String message) {
		super(message);
	}

	public PathGenerationException(Throwable cause) {
		super(cause);
	}

	public PathGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
