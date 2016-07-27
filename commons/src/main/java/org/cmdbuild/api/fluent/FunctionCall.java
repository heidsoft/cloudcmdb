package org.cmdbuild.api.fluent;

import java.util.Map;

public class FunctionCall extends ActiveFunction {

	FunctionCall(final FluentApiExecutor executor, final String functionName) {
		super(executor, functionName);
	}

	public FunctionCall with(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public Map<String, Object> execute() {
		return executor().execute(this);
	}

}