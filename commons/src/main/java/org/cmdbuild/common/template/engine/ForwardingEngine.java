package org.cmdbuild.common.template.engine;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEngine extends ForwardingObject implements Engine {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEngine() {
	}

	@Override
	protected abstract Engine delegate();

	@Override
	public Object eval(final String expression) {
		return delegate().eval(expression);
	}

}
