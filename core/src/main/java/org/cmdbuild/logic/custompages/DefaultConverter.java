package org.cmdbuild.logic.custompages;

import org.cmdbuild.data.store.custompage.DBCustomPage;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic.Converter;

public class DefaultConverter implements Converter {

	private final com.google.common.base.Converter<CustomPage, DBCustomPage> delegate;

	public DefaultConverter(final com.google.common.base.Converter<CustomPage, DBCustomPage> delegate) {
		this.delegate = delegate;
	}

	@Override
	public DBCustomPage toStore(final CustomPage value) {
		return delegate.convert(value);
	}

	@Override
	public CustomPage toLogic(final DBCustomPage value) {
		return delegate.reverse().convert(value);
	}

}