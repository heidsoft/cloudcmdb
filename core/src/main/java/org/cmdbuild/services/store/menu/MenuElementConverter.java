package org.cmdbuild.services.store.menu;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.GROUP_NAME_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.NUMBER_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.PARENT_ID_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.TYPE_ATTRIBUTE;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.BaseStorableConverter;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.store.menu.MenuElementImpl.MenuElementBuilder;

import com.google.common.collect.Maps;

public class MenuElementConverter extends BaseStorableConverter<MenuElement> {

	private final CMDataView dataView;
	private final DataAccessLogic dataAccessLogic;

	public MenuElementConverter(final CMDataView dataView, final UserDataAccessLogicBuilder userDataAccessLogicBuilder) {
		this.dataView = dataView;
		this.dataAccessLogic = userDataAccessLogicBuilder.build();
	}

	@Override
	public String getClassName() {
		return MENU_CLASS_NAME;
	}

	@Override
	public MenuElement convert(final CMCard card) {
		final MenuElementBuilder builder = MenuElementImpl.newInstance() //
				.withId(card.getId()) //
				.withDescription(String.class.cast(card.getDescription())) //
				.withGroupName(String.class.cast(card.get(GROUP_NAME_ATTRIBUTE))) //
				.withNumber(Integer.class.cast(card.get(NUMBER_ATTRIBUTE))) //
				.withParentId(Integer.class.cast(card.get(PARENT_ID_ATTRIBUTE))) //
				.withUuid(String.class.cast(card.getCode())) //
				.withType(MenuItemType.getType(card.get(TYPE_ATTRIBUTE, String.class)));

		if (!MenuItemType.FOLDER.getValue().equals(card.get(TYPE_ATTRIBUTE))) {
			final Long elementClass = card.get(ELEMENT_CLASS_ATTRIBUTE, Long.class);
			final String className = dataView.findClass(elementClass).getIdentifier().getLocalName();
			builder.withElementClassName(className);
			final Integer elementId = Integer.class.cast(card.get(ELEMENT_OBJECT_ID_ATTRIBUTE));
			builder.withElementId(elementId);

			if (MenuItemType.VIEW.equals(card.getType())) {
				final Card viewCard = dataAccessLogic.fetchCard(className, elementId.longValue());
				final Map<String, Object> specificTypeValues = Maps.newHashMap();
				specificTypeValues.put("type", viewCard.getAttribute("Type"));
				specificTypeValues.put("filter", viewCard.getAttribute("Filter"));
				specificTypeValues.put("sourceFunction", viewCard.getAttribute("SourceFunction"));
				final Long classId = (Long) viewCard.getAttribute("IdSourceClass");
				final CMClass cmClass = dataView.findClass(classId);
				if (cmClass != null) {
					specificTypeValues.put("sourceClassName", cmClass.getIdentifier().getLocalName());
				}
				builder.withSpecificTypeValues(specificTypeValues);
			}
		}
		return builder.build();
	}

	@Override
	public Map<String, Object> getValues(final MenuElement storable) {
		throw new UnsupportedOperationException(
				"This converter must be used only for reading. Writing is not supported.");
	}

}
