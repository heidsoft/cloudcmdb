package org.cmdbuild.config;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.dms.alfresco.AlfrescoDmsConfiguration;
import org.cmdbuild.dms.cmis.CmisDmsConfiguration;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.Settings;

public class DmsProperties extends DefaultProperties implements AlfrescoDmsConfiguration, CmisDmsConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "dms";

	private static final String ENABLED = "enabled";
	private static final String SERVICE = "dms.service.type";

	private static final String SERVER_URL = "server.url";
	private static final String FILE_SERVER_PORT = "fileserver.port";
	private static final String FILE_SERVER_URL = "fileserver.url";
	/*
	 * wspath is the path for the base space, fspath is the same thing, in terms
	 * of directories
	 */
	private static final String REPOSITORY_FS_PATH = "repository.fspath";
	private static final String REPOSITORY_WS_PATH = "repository.wspath";
	private static final String REPOSITORY_APP = "repository.app";
	private static final String PASSWORD = "credential.password";
	private static final String USER = "credential.user";
	private static final String CATEGORY_LOOKUP = "category.lookup";
	private static final String DELAY = "delay";
	private static final String ALFRESCO_CUSTOM_URI = "alfresco.custom.uri";
	private static final String ALFRESCO_CUSTOM_PREFIX = "alfresco.custom.prefix";
	private static final String ALFRESCO_CUSTOM_MODEL_FILENAME = "alfresco.custom.model.filename";
	private static final String METADATA_AUTOCOMPLETION_FILENAME = "metadata.autocompletion.filename";

	private static final String CMIS_URL = "dms.service.cmis.url";
	private static final String CMIS_USER = "dms.service.cmis.user";
	private static final String CMIS_PASSWORD = "dms.service.cmis.password";
	private static final String CMIS_PATH = "dms.service.cmis.path";
	private static final String CMIS_MODEL_TYPE = "dms.service.cmis.model";

	private static final Map<String, String> DEFAULTS;

	static {
		DEFAULTS = newHashMap();
		DEFAULTS.put(ENABLED, Boolean.FALSE.toString());
		DEFAULTS.put(SERVICE, "alfresco");
		DEFAULTS.put(SERVER_URL, "http://localhost:10080/alfresco/api");
		DEFAULTS.put(FILE_SERVER_PORT, "1121");
		DEFAULTS.put(FILE_SERVER_URL, "localhost");
		DEFAULTS.put(REPOSITORY_FS_PATH, "/Alfresco/User Homes/cmdbuild");
		DEFAULTS.put(REPOSITORY_WS_PATH, "/app:company_home/app:user_homes/");
		DEFAULTS.put(REPOSITORY_APP, "cm:cmdbuild");
		DEFAULTS.put(USER, "admin");
		DEFAULTS.put(PASSWORD, "admin");
		DEFAULTS.put(CATEGORY_LOOKUP, "AlfrescoCategory");
		DEFAULTS.put(DELAY, "1000");
		DEFAULTS.put(ALFRESCO_CUSTOM_URI, "org.cmdbuild.dms.alfresco");
		DEFAULTS.put(ALFRESCO_CUSTOM_PREFIX, "cmdbuild");
		DEFAULTS.put(ALFRESCO_CUSTOM_MODEL_FILENAME, "cmdbuildCustomModel.xml");
		DEFAULTS.put(METADATA_AUTOCOMPLETION_FILENAME, "metadataAutocompletion.xml");
		DEFAULTS.put(CMIS_URL, "http://localhost:10080/alfresco/api/-default-/public/cmis/versions/1.1/atom");
		DEFAULTS.put(CMIS_USER, "admin");
		DEFAULTS.put(CMIS_PASSWORD, "admin");
		DEFAULTS.put(CMIS_PATH, "/User Homes/cmdbuild");
		DEFAULTS.put(CMIS_MODEL_TYPE, "");
	}

	public static DmsProperties getInstance() {
		return (DmsProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	private final Collection<ChangeListener> changeListeners;

	public DmsProperties() {
		super();
		changeListeners = newHashSet();
		for (final Entry<String, String> entry : DEFAULTS.entrySet()) {
			setProperty(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void addListener(final ChangeListener listener) {
		changeListeners.add(listener);
	}

	@Override
	public void store() throws IOException {
		super.store();
		notifyListeners();
	}

	private void notifyListeners() {
		for (final ChangeListener changeListener : changeListeners) {
			changeListener.configurationChanged();
		}
	}

	@Override
	public boolean isEnabled() {
		final String enabled = getProperty(ENABLED, "false");
		return enabled.equals("true");
	}

	@Override
	public String getService() {
		return getProperty(SERVICE);
	}

	public void setService(final String value) {
		setProperty(SERVICE, value);
	}

	@Override
	public String getServerURL() {
		return getProperty(SERVER_URL);
	}

	public void setServerURL(final String url) {
		setProperty(SERVER_URL, url);
	}

	@Override
	public String getFtpPort() {
		return getProperty(FILE_SERVER_PORT);
	}

	public void setFtpPort(final String port) {
		setProperty(FILE_SERVER_PORT, port);
	}

	@Override
	public String getFtpHost() {
		return getProperty(FILE_SERVER_URL);
	}

	public void setFtpHost(final String hostname) {
		setProperty(FILE_SERVER_URL, hostname);
	}

	@Override
	public String getAlfrescoUser() {
		return getProperty(USER);
	}

	public void setAlfrescoUser(final String username) {
		setProperty(USER, username);
	}

	@Override
	public String getAlfrescoPassword() {
		return getProperty(PASSWORD);
	}

	public void setAlfrescoPassword(final String password) {
		setProperty(PASSWORD, password);
	}

	@Override
	public String getCmdbuildCategory() {
		return getProperty(CATEGORY_LOOKUP);
	}

	public void setCmdbuildCategory(final String category) {
		setProperty(CATEGORY_LOOKUP, category);
	}

	@Override
	public String getRepositoryFSPath() {
		return getProperty(REPOSITORY_FS_PATH);
	}

	public void setRepositoryFSPath(final String repository) {
		setProperty(REPOSITORY_FS_PATH, repository);
	}

	@Override
	public String getRepositoryWSPath() {
		return getProperty(REPOSITORY_WS_PATH);
	}

	public void setRepositoryWSPath(final String repository) {
		setProperty(REPOSITORY_WS_PATH, repository);
	}

	@Override
	public String getRepositoryApp() {
		return getProperty(REPOSITORY_APP);
	}

	public void setRepositoryApp(final String repository) {
		setProperty(REPOSITORY_APP, repository);
	}

	@Override
	public String getAlfrescoCustomUri() {
		return getProperty(ALFRESCO_CUSTOM_URI);
	}

	@Override
	public String getAlfrescoCustomPrefix() {
		return getProperty(ALFRESCO_CUSTOM_PREFIX);
	}

	@Override
	public String getCustomModelFileContent() {
		return contentOf(getProperty(ALFRESCO_CUSTOM_MODEL_FILENAME));
	}

	@Override
	public String getMetadataAutocompletionFileContent() {
		return contentOf(getProperty(METADATA_AUTOCOMPLETION_FILENAME));
	}

	private String contentOf(final String filename) {
		final File configurationPath = getPath();
		final File file = new File(configurationPath, filename);
		if (file.exists()) {
			try {
				final String content = FileUtils.readFileToString(file);
				return content;
			} catch (final IOException e) {
				final String message = format("error reading file '%s'", file);
				Log.DMS.error(message, e);
				return EMPTY;
			}
		} else {
			return EMPTY;
		}
	}

	@Override
	public long getDelayBetweenFtpAndWebserviceOperations() {
		return Long.valueOf(getProperty(DELAY));
	}

	@Override
	public String getCmisUrl() {
		return getProperty(CMIS_URL);
	}

	@Override
	public String getCmisUser() {
		return getProperty(CMIS_USER);
	}

	@Override
	public String getCmisPassword() {
		return getProperty(CMIS_PASSWORD);
	}

	@Override
	public String getCmisPath() {
		return getProperty(CMIS_PATH);
	}

	@Override
	public String getCmisModelType() {
		return getProperty(CMIS_MODEL_TYPE);
	}

}
