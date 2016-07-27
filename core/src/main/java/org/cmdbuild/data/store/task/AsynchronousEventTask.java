package org.cmdbuild.data.store.task;

public class AsynchronousEventTask extends Task {

	public static Builder<AsynchronousEventTask> newInstance() {
		return new Builder<AsynchronousEventTask>() {

			@Override
			protected AsynchronousEventTask doBuild() {
				return new AsynchronousEventTask(this);
			}

		};
	}

	private AsynchronousEventTask(final Builder<? extends Task> builder) {
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
