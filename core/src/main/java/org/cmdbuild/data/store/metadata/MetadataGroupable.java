package org.cmdbuild.data.store.metadata;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Code;
import static org.cmdbuild.data.store.Groupables.nameAndValue;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.data.store.ForwardingGroupable;
import org.cmdbuild.data.store.Groupable;

public class MetadataGroupable extends ForwardingGroupable {

	private static final String name = Code.getDBName();

	public static MetadataGroupable of(final CMAttribute attribute) {
		final CMEntryType owner = attribute.getOwner();
		final String value;
		if (owner != null) {
			final CMIdentifier identifier = owner.getIdentifier();
			final boolean skipNamespace = identifier.getNameSpace() == CMIdentifier.DEFAULT_NAMESPACE;
			value = new StringBuilder() //
					.append(skipNamespace ? EMPTY : format("%s.", identifier.getNameSpace())) //
					.append(format("%s.", identifier.getLocalName())) //
					.append(attribute.getName()) //
					.toString();
		} else {
			value = EMPTY;
		}
		return new MetadataGroupable(nameAndValue(name, value));
	}

	private final Groupable delegate;

	private MetadataGroupable(final Groupable delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Groupable delegate() {
		return delegate;
	}

}
