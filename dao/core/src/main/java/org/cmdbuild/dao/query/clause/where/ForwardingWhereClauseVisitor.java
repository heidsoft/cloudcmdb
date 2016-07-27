package org.cmdbuild.dao.query.clause.where;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingWhereClauseVisitor extends ForwardingObject implements WhereClauseVisitor {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingWhereClauseVisitor() {
	}

	@Override
	protected abstract WhereClauseVisitor delegate();

	@Override
	public void visit(final AndWhereClause whereClause) {
		delegate().visit(whereClause);
	}

	@Override
	public void visit(final EmptyWhereClause whereClause) {
		delegate().visit(whereClause);
	}

	@Override
	public void visit(final FalseWhereClause whereClause) {
		delegate().visit(whereClause);
	}

	@Override
	public void visit(final FunctionWhereClause whereClause) {
		delegate().visit(whereClause);
	}

	@Override
	public void visit(final NotWhereClause whereClause) {
		delegate().visit(whereClause);
	}

	@Override
	public void visit(final OrWhereClause whereClause) {
		delegate().visit(whereClause);
	}

	@Override
	public void visit(final SimpleWhereClause whereClause) {
		delegate().visit(whereClause);
	}

	@Override
	public void visit(final TrueWhereClause whereClause) {
		delegate().visit(whereClause);
	}

}
