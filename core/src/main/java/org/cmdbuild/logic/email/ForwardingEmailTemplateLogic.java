package org.cmdbuild.logic.email;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEmailTemplateLogic extends ForwardingObject implements EmailTemplateLogic {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEmailTemplateLogic() {
	}

	@Override
	protected abstract EmailTemplateLogic delegate();

	@Override
	public Iterable<Template> readAll() {
		return delegate().readAll();
	}

	@Override
	public Template read(final String name) {
		return delegate().read(name);
	}

	@Override
	public Long create(final Template template) {
		return delegate().create(template);
	}

	@Override
	public void update(final Template template) {
		delegate().update(template);
	}

	@Override
	public void delete(final String name) {
		delegate().delete(name);
	}

}
