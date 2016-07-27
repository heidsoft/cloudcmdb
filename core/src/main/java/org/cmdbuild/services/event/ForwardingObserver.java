package org.cmdbuild.services.event;

import org.cmdbuild.dao.entry.CMCard;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingObserver extends ForwardingObject implements Observer {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingObserver() {
	}

	@Override
	protected abstract Observer delegate();

	@Override
	public void afterCreate(final CMCard current) {
		delegate().afterCreate(current);
	}

	@Override
	public void beforeUpdate(final CMCard current, final CMCard next) {
		delegate().beforeUpdate(current, next);
	}

	@Override
	public void afterUpdate(final CMCard previous, final CMCard current) {
		delegate().afterUpdate(previous, current);
	}

	@Override
	public void beforeDelete(final CMCard current) {
		delegate().beforeDelete(current);
	}

}