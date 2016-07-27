package org.cmdbuild.model.data;

import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.unsupported;

import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.ForwardingRelation;
import org.cmdbuild.dao.entrytype.CMDomain;

/**
 * This class holds only the information to identify the relation: its Id and
 * the CMDomain that own it.
 */
public class IdentifiedRelation extends ForwardingRelation {

	private static final String UNSUPPORTED_OPERATION_MESSAGE = "You are tring to use an unsupported operation for class "
			+ IdentifiedRelation.class;
	private static final CMRelation UNSUPPORTED = newProxy(CMRelation.class, unsupported(UNSUPPORTED_OPERATION_MESSAGE));

	private final CMDomain domain;
	private final Long id;

	public IdentifiedRelation(final CMDomain domain, final Long id) {
		this.id = id;
		this.domain = domain;
	}

	@Override
	protected CMRelation delegate() {
		return UNSUPPORTED;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public CMDomain getType() {
		return domain;
	}

}
