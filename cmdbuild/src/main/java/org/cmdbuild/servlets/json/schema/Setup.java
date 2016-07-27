package org.cmdbuild.servlets.json.schema;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAMES;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.InstanceConverter;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jettison.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Setup extends JSONBaseWithSpringContext {

	private static final String INSTANCE_NAME = "instance_name";

	@JSONExported
	@Unauthorized
	public JSONObject getConfiguration( //
			@Parameter(NAME) final String module //
	) throws JSONException, AuthException, Exception {
		final JSONObject out = new JSONObject();
		out.put(DATA, readConfig(module));
		return out;
	}

	@JSONExported
	@Unauthorized
	public JSONObject getConfigurations( //
			@Parameter(NAMES) final String module //
	) throws JSONException, AuthException, Exception {
		final JSONObject out = new JSONObject();
		final JSONArray namesOfConfigFile = new JSONArray(module);
		for (int i = 0, l = namesOfConfigFile.length(); i < l; i++) {
			final Object nameAsObject = namesOfConfigFile.get(i);
			final String name = (String) nameAsObject;
			out.put(name, readConfig(name));
		}
		return out;
	}

	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveConfiguration( //
			@Parameter(NAME) final String module, //
			final Map<String, String> requestParams //
	) throws Exception {
		setUpLogic().save(module, requestParams);
	}

	private JSONObject readConfig(final String module) throws Exception {
		final Map<String, String> config = setUpLogic().load(module);
		final JSONObject data = new JSONObject();
		for (final Entry<String, String> entry : config.entrySet()) {
			if (entry.getKey().equals(INSTANCE_NAME)) {
				final TranslationObject translationObject = InstanceConverter.of(InstanceConverter.nameField()) //
						.create();
				final String translatedInstanceName = translationFacade().read(translationObject);
				data.put(entry.getKey(), defaultIfNull(translatedInstanceName, entry.getValue()));
			} else {
				data.put(entry.getKey(), entry.getValue());
			}
		}
		return data;
	}

}