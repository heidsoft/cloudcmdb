package org.cmdbuild.api.fluent;

public class ExecutorBasedFluentApi implements FluentApi {

	private final FluentApiExecutor executor;

	public ExecutorBasedFluentApi(final FluentApiExecutor executor) {
		this.executor = executor;
	}

	@Override
	public NewCard newCard(final String className) {
		return new NewCard(executor, className);
	}

	@Override
	public ExistingCard existingCard(final CardDescriptor descriptor) {
		return new ExistingCard(executor, descriptor.getClassName(), descriptor.getId());
	}

	@Override
	public ExistingCard existingCard(final String className, final int id) {
		return new ExistingCard(executor, className, id);
	}

	@Override
	public NewRelation newRelation(final String domainName) {
		return new NewRelation(executor, domainName);
	}

	@Override
	public ExistingRelation existingRelation(final String domainName) {
		return new ExistingRelation(executor, domainName);
	}

	@Override
	public QueryClass queryClass(final String className) {
		return new QueryClass(executor, className);
	}

	@Override
	public FunctionCall callFunction(final String functionName) {
		return new FunctionCall(executor, functionName);
	}

	@Override
	public CreateReport createReport(final String title, final String format) {
		return new CreateReport(executor, title, format);
	}

	@Override
	public ActiveQueryRelations queryRelations(final CardDescriptor descriptor) {
		return new ActiveQueryRelations(executor, descriptor.getClassName(), descriptor.getId());
	}

	@Override
	public ActiveQueryRelations queryRelations(final String className, final int id) {
		return new ActiveQueryRelations(executor, className, id);
	}

	@Override
	public NewProcessInstance newProcessInstance(final String processClassName) {
		return new NewProcessInstance(executor, processClassName);
	}

	@Override
	public ExistingProcessInstance existingProcessInstance(final String processClassName, final int processId) {
		return new ExistingProcessInstance(executor, processClassName, processId);
	}

	@Override
	public QueryAllLookup queryLookup(final String type) {
		return new QueryAllLookup(executor, type);
	}

}
