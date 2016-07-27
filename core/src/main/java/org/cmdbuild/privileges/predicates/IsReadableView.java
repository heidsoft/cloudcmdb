package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;

import java.util.NoSuchElementException;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.view.View;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class IsReadableView implements Predicate<CMCard> {

	private final PrivilegeContext privilegeContext;
	private final CMDataView dataView;
	private final StorableConverter<View> viewConverter;

	public IsReadableView( //
			final CMDataView dataView, //
			final PrivilegeContext privilegeContext, //
			final StorableConverter<View> viewConverter //
	) {
		this.privilegeContext = privilegeContext;
		this.dataView = dataView;
		this.viewConverter = viewConverter;
	}

	@Override
	public boolean apply(final CMCard menuCard) {
		final Integer viewId = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE, Integer.class);
		if (viewId == null) {
			return false;
		}
		final DataViewStore<View> store = DataViewStore.newInstance(dataView, viewConverter);
		View fetchedView = null;

		try {
			fetchedView = store.read(storableOf(viewId.toString()));
		} catch (final NoSuchElementException e) {
			final Marker marker = MarkerFactory.getMarker(IsReadableView.class.getName());
			Log.CMDBUILD.debug(marker, "No such the View {} looking if it is readable", viewId.toString());
		}

		if (fetchedView == null) {
			return false;
		}

		return privilegeContext.hasReadAccess(fetchedView);
	}

}
