package com.ryanberdeen.routes;

public class PathBuilder {
	private int requiredLength = 0;
	private StringBuilder stringBuilder = new StringBuilder();

	public void append(Object o, boolean required) {
		String string = o.toString();
		stringBuilder.append(string);
		if (required) {
			requiredLength += string.length();
		}
	}

	@Override
	public String toString() {
		return stringBuilder.toString().substring(0, requiredLength);
	}
}