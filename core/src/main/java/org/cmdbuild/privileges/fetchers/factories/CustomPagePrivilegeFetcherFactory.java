package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.privileges.fetchers.CustomPagePrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;

public class CustomPagePrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final CMDataView dataView;
	private final CustomPagesLogic customPagesLogic;
	private Long groupId;

	public CustomPagePrivilegeFetcherFactory(final CMDataView dataView, final CustomPagesLogic customPagesLogic) {
		this.dataView = dataView;
		this.customPagesLogic = customPagesLogic;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new CustomPagePrivilegeFetcher(dataView, groupId, customPagesLogic);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
