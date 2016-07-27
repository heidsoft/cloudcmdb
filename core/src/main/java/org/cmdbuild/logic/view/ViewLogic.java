package org.cmdbuild.logic.view;

import static org.cmdbuild.data.store.Storables.storableOf;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.model.view.View.ViewType;
import org.cmdbuild.privileges.GrantCleaner;

public class ViewLogic implements Logic {

	private final CMDataView dataView;
	private final Store<View> store;
	private final OperationUser operationUser;
	private final GrantCleaner grantCleaner;

	public ViewLogic( //
			final CMDataView dataView, //
			final StorableConverter<View> converter, //
			final OperationUser operationUser //
	) {
		this.dataView = dataView;
		this.store = DataViewStore.newInstance(dataView, converter);
		this.operationUser = operationUser;
		this.grantCleaner = new GrantCleaner(dataView);
	}

	public List<View> fetchViewsOfAllTypes() {
		final List<View> views = new ArrayList<View>();
		for (final View view : store.readAll()) {
			if ((operationUser.hasAdministratorPrivileges() || operationUser.hasReadAccess(view))) {
				if (view.getType().equals(ViewType.FILTER)) {
					if (isActive(view.getSourceClassName())) {
						views.add(view);
					}
				} else {
					views.add(view);
				}
			}
		}
		return views;
	}

	private boolean isActive(final String sourceClassName) {
		final CMClass clazz = dataView.findClass(sourceClassName);
		return clazz.isActive();
	}

	public List<View> read(final View.ViewType type) {
		final List<View> views = new ArrayList<View>();
		for (final View view : fetchViewsOfAllTypes()) {
			if (view.getType().equals(type)) {
				views.add(view);
			}
		}
		return views;
	}

	public View read(final Long id) {
		return store.read(storableOf(id));
	}

	public void create(final View view) {
		store.create(view);
	}

	public void update(final View view) {
		store.update(view);
	}

	public void delete(final Long id) {
		store.delete(storableOf(id));
		grantCleaner.deleteGrantReferingTo(id);
	}

}
