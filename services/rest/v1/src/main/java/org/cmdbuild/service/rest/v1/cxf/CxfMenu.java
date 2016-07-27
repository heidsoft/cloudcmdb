package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.v1.model.Models.newMenu;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;
import static org.cmdbuild.services.store.menu.Comparators.byIndex;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.service.rest.v1.Menu;
import org.cmdbuild.service.rest.v1.model.MenuDetail;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.cmdbuild.services.store.menu.MenuItem;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Ordering;

public class CxfMenu implements Menu {

	private static final class MenuItemToMenuDetail implements Function<MenuItem, MenuDetail> {

		private final CMDataView dataView;

		public MenuItemToMenuDetail(final CMDataView dataView) {
			this.dataView = dataView;
		}

		@Override
		public MenuDetail apply(final MenuItem input) {
			final CMClass referencedClass = dataView.findClass(input.getReferedClassName());
			return newMenu() //
					.withMenuType(input.getType().getValue()) // TODO translate
					.withIndex(Long.valueOf(input.getIndex())) //
					.withObjectType((referencedClass == null) ? null : referencedClass.getName()) //
					.withObjectId(input.getReferencedElementId().longValue()) //
					.withObjectDescription(input.getDescription()) //
					.withChildren( //
							from( //
									Ordering.from(byIndex()) //
											.sortedCopy(input.getChildren()) //
							) //
							.transform(this) //
									.toList()) //
					.build();
		}

	};

	private final Supplier<String> currentGroupSupplier;
	private final MenuLogic menuLogic;
	private final CMDataView dataView;

	public CxfMenu(final Supplier<String> currentGroupSupplier, final MenuLogic menuLogic, final CMDataView dataView) {
		this.currentGroupSupplier = currentGroupSupplier;
		this.menuLogic = menuLogic;
		this.dataView = dataView;
	}

	@Override
	public ResponseSingle<MenuDetail> read() {
		final String group = currentGroupSupplier.get();
		final MenuItem menuItem = menuLogic.readMenuWithPrivileges(group);
		final MenuDetail element = new MenuItemToMenuDetail(dataView).apply(menuItem);
		return newResponseSingle(MenuDetail.class) //
				.withElement(element) //
				.build();
	}

}
