package com.ryanberdeen.routes.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourceTemplate implements RouteSetBuilderTemplate {
	public static final String COLLECTION_PATTERN_OPTION = "collectionPattern";
	public static final String MEMBER_PATTERN_OPTION = "memberPattern";

	// resource actions
	public static final String INDEX_ACTION = "index";
	public static final String CREATE_ACTION = "create";
	public static final String SHOW_ACTION = "show";
	public static final String EDIT_ACTION = "edit";
	public static final String UPDATE_ACTION = "update";
	public static final String DESTROY_ACTION = "destroy";

	// methods
	private static final String POST_METHOD = "POST";
	private static final String GET_METHOD = "GET";
	private static final String PUT_METHOD = "PUT";
	private static final String DELETE_METHOD = "DELETE";

	private ArrayList<String> collectionActions;
	private HashMap<String, String> collectionActionMethods;

	private ArrayList<String> memberActions;
	private HashMap<String, String> memberActionMethods;

	public ResourceTemplate() {
		collectionActions = new ArrayList<String>();
		collectionActions.add(INDEX_ACTION);

		collectionActionMethods = new HashMap<String, String>();
		collectionActionMethods.put(POST_METHOD, CREATE_ACTION);

		memberActions = new ArrayList<String>();
		memberActions.add(SHOW_ACTION);
		memberActions.add(EDIT_ACTION);

		memberActionMethods = new HashMap<String, String>();
		memberActionMethods.put(GET_METHOD, SHOW_ACTION);
		memberActionMethods.put(PUT_METHOD, UPDATE_ACTION);
		memberActionMethods.put(DELETE_METHOD, DESTROY_ACTION);
	}

	public void applyTemplate(RouteSetBuilder routeSetBuilder) {
		routeSetBuilder.setTemplate("collection", new CollectionTemplate());
		routeSetBuilder.setTemplate("member", new MemberTemplate());
	}

	public String getActionParamterName() {
		return "action";
	}

	private class CollectionTemplate implements RouteSetBuilderTemplate {
		public void applyTemplate(RouteSetBuilder routeSetBuilder) {
			String pattern = routeSetBuilder.getOption(COLLECTION_PATTERN_OPTION);
			if (pattern == null) {
				pattern = ":action";
				routeSetBuilder.setOption(COLLECTION_PATTERN_OPTION, pattern);
			}

			routeSetBuilder.setParameterValue(getActionParamterName(), INDEX_ACTION);
			routeSetBuilder.setDefaultStaticParameterValue(getActionParamterName(), INDEX_ACTION);

			Map<String, String> appliedParameters;
			routeSetBuilder.append(pattern);
			for (String action : collectionActions) {
				appliedParameters = Collections.singletonMap(getActionParamterName(), action);
				routeSetBuilder.apply(appliedParameters);
			}
		}
	}

	private class MemberTemplate implements RouteSetBuilderTemplate {
		public void applyTemplate(RouteSetBuilder routeSetBuilder) {
			String pattern = routeSetBuilder.getOption(MEMBER_PATTERN_OPTION);
			if (pattern == null) {
				pattern = ":id/:action";
				routeSetBuilder.setOption(MEMBER_PATTERN_OPTION, pattern);
			}

			routeSetBuilder.setParameterValue(getActionParamterName(), SHOW_ACTION);
			routeSetBuilder.setDefaultStaticParameterValue(getActionParamterName(), SHOW_ACTION);

			Map<String, String> appliedParameters;
			routeSetBuilder.append(pattern);
			for (String action : memberActions) {
				appliedParameters = Collections.singletonMap(getActionParamterName(), action);
				routeSetBuilder.apply(appliedParameters);
			}
		}
	}
}
