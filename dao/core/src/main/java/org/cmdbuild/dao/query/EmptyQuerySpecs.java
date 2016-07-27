package org.cmdbuild.dao.query;

import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.unsupported;

import org.cmdbuild.dao.query.clause.from.FromClause;

public class EmptyQuerySpecs extends ForwardingQuerySpecs {

	private static final QuerySpecs UNSUPPORTED = newProxy(QuerySpecs.class, unsupported("method not supported"));

	@Override
	protected QuerySpecs delegate() {
		return UNSUPPORTED;
	}

	@Override
	public FromClause getFromClause() {
		// don't change it
		return null;
	}

}
