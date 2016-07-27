package org.cmdbuild.data.store.task;

public class GenericTaskDefinition extends TaskDefinition {

	public static Builder<GenericTaskDefinition> newInstance() {
		return new Builder<GenericTaskDefinition>() {

			@Override
			protected GenericTaskDefinition doBuild() {
				return new GenericTaskDefinition(this);
			}

		};
	}

	private GenericTaskDefinition(final Builder<? extends TaskDefinition> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskDefinitionVisitor visitor) {
		visitor.visit(this);
	}

}
