package com.ryanberdeen.routes.builder;

public interface RouteOptions {
	public abstract RouteBuilder setOption(String optionName, String value);

	public abstract RouteBuilder setParameterValue(String name, String value);

	public abstract RouteBuilder setDefaultStaticParameterValue(String name, String value);

	public abstract RouteBuilder setParameterRegex(String name, String regex);
}