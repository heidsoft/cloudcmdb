package org.cmdbuild.privileges.predicates;

import static java.lang.String.format;
import static org.cmdbuild.logger.Log.CMDBUILD;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;

import java.util.NoSuchElementException;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logic.custompages.CustomPage;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.privileges.CustomPageAdapter;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class IsReadableCustomPage implements Predicate<CMCard> {

	private static final Logger logger = CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(IsReadableCustomPage.class.getName());

	private final PrivilegeContext privilegeContext;
	private final CustomPagesLogic logic;

	public IsReadableCustomPage(final PrivilegeContext privilegeContext, final CustomPagesLogic logic) {
		this.privilegeContext = privilegeContext;
		this.logic = logic;
	}

	@Override
	public boolean apply(final CMCard menuCard) {
		final Integer id = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE, Integer.class);
		if (id == null) {
			return false;
		}

		try {
			logger.debug(marker, "reading element with id '{}'", id);
			final CustomPage element = logic.read(id.longValue());
			return privilegeContext.hasReadAccess(new CustomPageAdapter(element));
		} catch (final NoSuchElementException e) {
			logger.error(marker, format("element '%d' not found", id), e);
			return false;
		}
	}

}
