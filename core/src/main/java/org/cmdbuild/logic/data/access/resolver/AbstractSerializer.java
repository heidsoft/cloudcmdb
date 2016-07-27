package org.cmdbuild.logic.data.access.resolver;

import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver.EntryFiller;

public abstract class AbstractSerializer<T extends CMEntry> extends ForwardingAttributeTypeVisitor {

	private static final NullAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

	protected Object rawValue;
	protected String attributeName;
	protected EntryFiller<T> entryFiller;

	@Override
	protected final CMAttributeTypeVisitor delegate() {
		return DELEGATE;
	}

	public void setRawValue(final Object rawValue) {
		this.rawValue = rawValue;
	}

	public void setAttributeName(final String attributeName) {
		this.attributeName = attributeName;
	}

	public void setEntryFiller(final EntryFiller<T> entryFiller) {
		this.entryFiller = entryFiller;
	}

	protected void setAttribute(final String name, final Object value) {
		entryFiller.setValue(name, value);
	}

}
