package org.cmdbuild.dms;

import static java.util.Collections.emptyList;

import java.util.List;

import org.cmdbuild.dms.exception.DmsError;

public class ConfigurationAwareDmsService extends ForwardingDmsService {

	private static final List<StoredDocument> EMPTY = emptyList();

	private final DmsService delegate;
	private final DmsConfiguration dmsConfiguration;

	public ConfigurationAwareDmsService(final DmsService delegate, final DmsConfiguration dmsConfiguration) {
		this.delegate = delegate;
		this.dmsConfiguration = dmsConfiguration;
	}

	@Override
	protected DmsService delegate() {
		return delegate;
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		return dmsConfiguration.isEnabled() ? super.search(document) : EMPTY;
	}

}