package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.model.view.View;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.ViewPrivilegeFetcher;

public class ViewPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final CMDataView dataView;
	private final StorableConverter<View> viewConverter;
	private Long groupId;

	public ViewPrivilegeFetcherFactory(final CMDataView view, final StorableConverter<View> viewConverter) {
		this.dataView = view;
		this.viewConverter = viewConverter;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new ViewPrivilegeFetcher(dataView, groupId, viewConverter);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
