package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.ryanberdeen.routes.Route;
import com.ryanberdeen.routes.UrlPattern;

public class RouteBuilder implements Cloneable {
	private static final String NAME = "name";
	private static final String NAME_PREFIX = "namePrefix";
	private static final String PATTERN = "pattern";
	private static final String PATTERN_PREFIX = "patternPrefix";
	private static final String COLLECTION_PATTERN = "collectionPattern";
	private static final String MEMBER_PATTERN = "memberPattern";
	private static final String METHODS = "methods";
	private static final String EXCLUDED_METHODS = "excludedMethods";

	// resource actions
	private static final String INDEX_ACTION = "index";
	private static final String CREATE_ACTION = "create";
	private static final String SHOW_ACTION = "show";
	private static final String EDIT_ACTION = "edit";
	private static final String UPDATE_ACTION = "update";
	private static final String DESTROY_ACTION = "destroy";

	// methods
	private static final String POST_METHOD = "POST";
	private static final String GET_METHOD = "GET";
	private static final String PUT_METHOD = "PUT";
	private static final String DELETE_METHOD = "DELETE";

	public HashMap<String, String> parameterValues;
	public HashMap<String, String> defaultStaticParameterValues;
	public HashMap<String, String> parameterRegexes;

	public ArrayList<String> defaultResourceCollectionActions;
	public HashMap<String, String> defaultResourceCollectionActionMethods;

	public HashMap<String, String> defaultResourceMemberActionMethods;
	public ArrayList<String> defaultResourceMemberActions;

	private String name;
	private String namePrefix;

	private String pattern;
	private String patternPrefix;

	private String collectionPattern;
	private String memberPattern;

	private HashSet<String> methods;
	private HashSet<String> excludedMethods;

	public RouteBuilder() {
		initDefault();
	}

	public RouteBuilder(RouteBuilder that) {
		if (that != null) {
			parameterValues = new HashMap<String, String>(that.parameterValues);
			defaultStaticParameterValues = new HashMap<String, String>(that.defaultStaticParameterValues);
			parameterRegexes = new HashMap<String, String>(that.parameterRegexes);
			defaultResourceMemberActionMethods = new HashMap<String, String>(that.defaultResourceMemberActionMethods);
			defaultResourceCollectionActions = new ArrayList<String>(that.defaultResourceCollectionActions);
			defaultResourceMemberActions = new ArrayList<String>(that.defaultResourceMemberActions);

			name = that.name;
			namePrefix = that.namePrefix;

			pattern = that.patternPrefix;
			patternPrefix = that.patternPrefix;

			collectionPattern = that.collectionPattern;
			memberPattern = that.memberPattern;

			methods = new HashSet<String>(that.methods);
			excludedMethods = new HashSet<String>(that.excludedMethods);

		}
		else {
			initDefault();
		}
	}

	private void initDefault() {
		parameterValues = new HashMap<String, String>();
		defaultStaticParameterValues = new HashMap<String, String>();
		parameterRegexes = new HashMap<String, String>();

		defaultResourceCollectionActions = new ArrayList<String>();
		defaultResourceCollectionActions.add(INDEX_ACTION);

		defaultResourceCollectionActionMethods = new HashMap<String, String>();
		defaultResourceCollectionActionMethods.put(POST_METHOD, CREATE_ACTION);

		defaultResourceMemberActions = new ArrayList<String>();
		defaultResourceMemberActions.add(SHOW_ACTION);
		defaultResourceMemberActions.add(EDIT_ACTION);

		defaultResourceMemberActionMethods = new HashMap<String, String>();
		defaultResourceMemberActionMethods.put(GET_METHOD, SHOW_ACTION);
		defaultResourceMemberActionMethods.put(PUT_METHOD, UPDATE_ACTION);
		defaultResourceMemberActionMethods.put(DELETE_METHOD, DESTROY_ACTION);

		name = null;
		namePrefix = "";

		pattern = null;
		patternPrefix = "";

		collectionPattern = ":action";
		memberPattern = ":id/:action";

		methods = new HashSet<String>();
		excludedMethods = new HashSet<String>();
	}

	@Override
	protected RouteBuilder clone() {
		return new RouteBuilder(this);
	}

	public String getPattern() {
		return patternPrefix + pattern;
	}

	public UrlPattern createUrlPattern() {
		return UrlPattern.parse(getPattern(), parameterValues.keySet(), parameterRegexes);
	}

	public Route createRoute() {
		Route route = new Route(getPattern(), parameterValues, parameterRegexes);
		route.setDefaultStaticParameters(defaultStaticParameterValues);
		route.setName(getName());
		route.setMethods(getMethods());
		route.setExcludedMethods(getExcludedMethods());
		return route;
	}

	public HashSet<String> getMethods() {
		return methods;
	}

	public HashSet<String> getExcludedMethods() {
		return excludedMethods;
	}

	public void setOption(String optionName, String value) {
		if (NAME.equals(optionName)) {
			name = value;
		}
		else if (NAME_PREFIX.equals(optionName)) {
			namePrefix = optionName;
		}
		else if (PATTERN.equals(optionName)) {
			pattern = value;
		}
		else if (PATTERN_PREFIX.equals(optionName)) {
			patternPrefix = value;
		}
		else if (COLLECTION_PATTERN.equals(optionName)) {
			collectionPattern = value;
		}
		else if (MEMBER_PATTERN.equals(optionName)) {
			memberPattern = value;
		}
		else if (METHODS.equals(optionName)) {
			methods = parseMethodString(value);
		}
		else if (EXCLUDED_METHODS.equals(optionName)) {
			excludedMethods = parseMethodString(value);
		}
		else {
			// TODO exception type
			throw new RuntimeException("Invalid parameter name: " + optionName);
		}
	}

	private static HashSet<String> parseMethodString(String value) {
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

	public String getName() {
		String result = name;
		if (name != null) {
			result = namePrefix + result;
		}

		return result;
	}

	public String getCollectionPattern() {
		return collectionPattern;
	}

	public String getMemberPattern() {
		return memberPattern;
	}

	public HashMap<String, String> getDefaultResourceMemberActionMethods() {
		return defaultResourceMemberActionMethods;
	}

	public RouteBuilder createMemberBuilder() {
		RouteBuilder result = clone();
		result.parameterValues.put(getActionParamterName(), SHOW_ACTION);
		result.defaultStaticParameterValues.put(getActionParamterName(), SHOW_ACTION);
		return result;
	}

	public RouteBuilder createCollectionBuilder() {
		RouteBuilder result = clone();
		result.parameterValues.put(getActionParamterName(), INDEX_ACTION);
		result.defaultStaticParameterValues.put(getActionParamterName(), INDEX_ACTION);
		return result;
	}

	public String getActionParamterName() {
		return "action";
	}
}
