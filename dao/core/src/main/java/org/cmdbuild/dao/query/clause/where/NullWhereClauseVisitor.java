package org.cmdbuild.dao.query.clause.where;

public class NullWhereClauseVisitor implements WhereClauseVisitor {

	private static final NullWhereClauseVisitor INSTANCE = new NullWhereClauseVisitor();

	public static NullWhereClauseVisitor getInstance() {
		return INSTANCE;
	}

	private NullWhereClauseVisitor() {
		// use factory method
	}

	@Override
	public void visit(final AndWhereClause whereClause) {
		// nothing to do
	}

	@Override
	public void visit(final EmptyWhereClause whereClause) {
		// nothing to do
	}

	@Override
	public void visit(final FalseWhereClause whereClause) {
		// nothing to do
	}

	@Override
	public void visit(final FunctionWhereClause whereClause) {
		// nothing to do
	}

	@Override
	public void visit(final NotWhereClause whereClause) {
		// nothing to do
	}

	@Override
	public void visit(final OrWhereClause whereClause) {
		// nothing to do
	}

	@Override
	public void visit(final SimpleWhereClause whereClause) {
		// nothing to do
	}

	@Override
	public void visit(final TrueWhereClause whereClause) {
		// nothing to do
	}

}
