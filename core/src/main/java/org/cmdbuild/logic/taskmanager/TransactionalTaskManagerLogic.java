package org.cmdbuild.logic.taskmanager;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalTaskManagerLogic extends ForwardingTaskManagerLogic {

	private final TaskManagerLogic delegate;

	public TransactionalTaskManagerLogic(final TaskManagerLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected TaskManagerLogic delegate() {
		return delegate;
	}

	@Override
	@Transactional
	public Long create(final Task task) {
		return super.create(task);
	}

	@Override
	@Transactional
	public void update(final Task task) {
		super.update(task);
	}

	@Override
	@Transactional
	public void delete(final Task task) {
		super.delete(task);
	}

	@Override
	@Transactional
	public void activate(final Long id) {
		super.activate(id);
	}

	@Override
	@Transactional
	public void deactivate(final Long id) {
		super.deactivate(id);
	}

}