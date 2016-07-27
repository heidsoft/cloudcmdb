package org.cmdbuild.model.widget;

import org.cmdbuild.model.widget.customform.CustomForm;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingWidgetVisitor extends ForwardingObject implements WidgetVisitor {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingWidgetVisitor() {
	}

	@Override
	protected abstract WidgetVisitor delegate();

	@Override
	public void visit(final Calendar widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final CreateModifyCard widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final CustomForm widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final Grid widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final LinkCards widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final ManageEmail widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final NavigationTree widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final ManageRelation widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final OpenAttachment widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final OpenNote widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final OpenReport widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final Ping widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final PresetFromCard widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final WebService widget) {
		delegate().visit(widget);
	}

	@Override
	public void visit(final Workflow widget) {
		delegate().visit(widget);
	}

}
