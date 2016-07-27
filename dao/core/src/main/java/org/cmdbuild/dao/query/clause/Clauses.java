package org.cmdbuild.dao.query.clause;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;

public class Clauses {

	public static FunctionCall call(final CMFunction function, final Object... actualParameters) {
		return new FunctionCall(function, newArrayList(actualParameters));
	}

	public static FunctionCall call(final CMFunction function, final Iterable<Object> actualParameters) {
		return new FunctionCall(function, newArrayList(actualParameters));
	}

	public static FunctionCall call(final CMFunction function, final Map<String, Object> actualParametersMap) {
		final List<Object> actualParameters = newArrayList();
		for (final CMFunctionParameter element : function.getInputParameters()) {
			actualParameters.add(actualParametersMap.get(element.getName()));
		}
		return new FunctionCall(function, actualParameters);
	}

	private Clauses() {
		// prevents instantiation
	}

}
