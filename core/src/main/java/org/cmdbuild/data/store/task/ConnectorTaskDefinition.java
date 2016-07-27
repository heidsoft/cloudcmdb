package org.cmdbuild.data.store.task;

public class ConnectorTaskDefinition extends TaskDefinition {

	public static Builder<ConnectorTaskDefinition> newInstance() {
		return new Builder<ConnectorTaskDefinition>() {

			@Override
			protected ConnectorTaskDefinition doBuild() {
				return new ConnectorTaskDefinition(this);
			}

		};
	}

	private ConnectorTaskDefinition(final Builder<? extends TaskDefinition> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskDefinitionVisitor visitor) {
		visitor.visit(this);
	}

}
