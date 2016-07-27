package org.cmdbuild.logic.data.access.filter.model;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingElementVisitor extends ForwardingObject implements ElementVisitor {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingElementVisitor() {
	}

	@Override
	protected abstract ElementVisitor delegate();

	@Override
	public void visit(final All element) {
		delegate().visit(element);
	}

	@Override
	public void visit(final Attribute element) {
		delegate().visit(element);
	}

	@Override
	public void visit(final OneOf element) {
		delegate().visit(element);
	}

}
