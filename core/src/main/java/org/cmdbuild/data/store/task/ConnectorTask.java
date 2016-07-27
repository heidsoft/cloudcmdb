package org.cmdbuild.data.store.task;

public class ConnectorTask extends Task {

	public static Builder<ConnectorTask> newInstance() {
		return new Builder<ConnectorTask>() {

			@Override
			protected ConnectorTask doBuild() {
				return new ConnectorTask(this);
			}

		};
	}

	private ConnectorTask(final Builder<? extends Task> builder) {
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
