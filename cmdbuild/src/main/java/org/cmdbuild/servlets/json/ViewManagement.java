package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_FUNCTION;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.model.view.View;
import org.cmdbuild.model.view.ViewImpl;
import org.cmdbuild.servlets.json.serializers.ViewSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewManagement extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONObject read() throws JSONException {
		return new ViewSerializer().toClient(viewLogic().fetchViewsOfAllTypes());
	}

	@JSONExported
	public void createSQLView( //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = SOURCE_FUNCTION) final String sourceFunction //
	) {
		viewLogic().create(fillSQLView(null, name, description, sourceFunction));
	}

	@JSONExported
	public JSONObject readSQLView() throws JSONException {
		return new ViewSerializer().toClient(viewLogic().read(View.ViewType.SQL));
	}

	@JSONExported
	public void updateSQLView( //
			@Parameter(value = ID) final Long id, //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = SOURCE_FUNCTION) final String sourceFunction //
	) {
		viewLogic().update(fillSQLView(id, name, description, sourceFunction));
	}

	@JSONExported
	public void deleteSqlView( //
			@Parameter(value = ID) final Long id //
	) {
		viewLogic().delete(id);
	}

	@JSONExported
	public void createFilterView( //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = FILTER) final String filter, //
			@Parameter(value = SOURCE_CLASS_NAME) final String className //
	) { //
		viewLogic().create(fillFilterView(null, name, description, className, filter));
	}

	@JSONExported
	public JSONObject readFilterView() throws JSONException {
		return new ViewSerializer().toClient(viewLogic().read(View.ViewType.FILTER));
	}

	@JSONExported
	public void updateFilterView( //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = FILTER) final String filter, //
			@Parameter(value = ID) final Long id, //
			@Parameter(value = SOURCE_CLASS_NAME) final String className //
	) { //
		viewLogic().update(fillFilterView(id, name, description, className, filter));
	}

	@JSONExported
	public void deleteFilterView( //
			@Parameter(value = ID) final Long id //
	) {
		viewLogic().delete(id);
	}

	private View fillFilterView( //
			final Long id, //
			final String name, //
			final String description, //
			final String className, //
			final String filter //
	) {
		// TODO move within logic
		Validate.notBlank(name, "invalid name");
		Validate.notBlank(className, "invalid class name");
		Validate.notBlank(filter, "invalid filter");
		final ViewImpl view = new ViewImpl();
		view.setId(id);
		view.setName(name);
		view.setDescription(description);
		view.setSourceClassName(className);
		view.setType(View.ViewType.FILTER);
		view.setFilter(filter);
		return view;
	}

	private View fillSQLView( //
			final Long id, //
			final String name, //
			final String description, //
			final String sourceFunction //
	) {
		// TODO move within logic
		Validate.notBlank(name, "invalid name");
		Validate.notBlank(sourceFunction, "invalid function name");
		final ViewImpl view = new ViewImpl();
		view.setId(id);
		view.setName(name);
		view.setDescription(description);
		view.setType(View.ViewType.SQL);
		view.setSourceFunction(sourceFunction);
		return view;
	}

}