package org.cmdbuild.data.store.task;

public class AsynchronousEventTaskDefinition extends TaskDefinition {

	public static Builder<AsynchronousEventTaskDefinition> newInstance() {
		return new Builder<AsynchronousEventTaskDefinition>() {

			@Override
			protected AsynchronousEventTaskDefinition doBuild() {
				return new AsynchronousEventTaskDefinition(this);
			}

		};
	}

	private AsynchronousEventTaskDefinition(final Builder<? extends TaskDefinition> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskDefinitionVisitor visitor) {
		visitor.visit(this);
	}

}
