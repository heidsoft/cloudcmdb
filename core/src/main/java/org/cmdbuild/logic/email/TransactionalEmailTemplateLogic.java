package org.cmdbuild.logic.email;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalEmailTemplateLogic extends ForwardingEmailTemplateLogic {

	private final EmailTemplateLogic delegate;

	public TransactionalEmailTemplateLogic(final EmailTemplateLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected EmailTemplateLogic delegate() {
		return delegate;
	}

	@Transactional
	@Override
	public Long create(final Template template) {
		return super.create(template);
	}

	@Transactional
	@Override
	public void update(final Template template) {
		super.update(template);
	}

	@Transactional
	@Override
	public void delete(final String name) {
		super.delete(name);
	}

}
