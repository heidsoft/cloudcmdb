package org.cmdbuild.dao.query.clause.where;

public interface OperatorAndValueVisitor {

	void visit(BeginsWithOperatorAndValue operatorAndValue);

	void visit(ContainsOperatorAndValue operatorAndValue);

	void visit(EmptyArrayOperatorAndValue operatorAndValue);

	void visit(EndsWithOperatorAndValue operatorAndValue);

	void visit(EqualsOperatorAndValue operatorAndValue);

	void visit(GreaterThanOperatorAndValue operatorAndValue);

	void visit(GreaterThanOrEqualToOperatorAndValue operatorAndValue);

	void visit(InOperatorAndValue operatorAndValue);

	void visit(LessThanOperatorAndValue operatorAndValue);

	void visit(LessThanOrEqualToOperatorAndValue operatorAndValue);

	void visit(NetworkContained operatorAndValue);

	void visit(NetworkContainedOrEqual operatorAndValue);

	void visit(NetworkContains operatorAndValue);

	void visit(NetworkContainsOrEqual operatorAndValue);

	void visit(NetworkRelationed operatorAndValue);

	void visit(NullOperatorAndValue operatorAndValue);

	void visit(StringArrayOverlapOperatorAndValue operatorAndValue);

}
