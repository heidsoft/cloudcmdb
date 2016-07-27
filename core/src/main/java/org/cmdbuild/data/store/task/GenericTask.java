package org.cmdbuild.data.store.task;

public class GenericTask extends Task {

	public static Builder<GenericTask> newInstance() {
		return new Builder<GenericTask>() {

			@Override
			protected GenericTask doBuild() {
				return new GenericTask(this);
			}

		};
	}

	private GenericTask(final Builder<? extends Task> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Builder<? extends Task> builder() {
		return newInstance();
	}

}
