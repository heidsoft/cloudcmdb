package org.cmdbuild.services;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.auth.Login;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.listeners.ValuesStore;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * Should be merged with the RequestListener
 */

public class SessionVars implements AuthenticationStore, LanguageStore {

	private static final String AUTH_TYPE_KEY = "authType";
	private static final String LANGUAGE_KEY = "language";
	private static final String LOGIN_KEY = "login";
	private static final String REPORTFACTORY_KEY = "ReportFactorySessionObj";
	private static final String NEWREPORT_KEY = "newReport";
	private static final String CSVDATA_KEY = "csvdata";

	private final ValuesStore valuesStore;
	private final CmdbuildConfiguration configuration;

	@Autowired
	public SessionVars(final ValuesStore valuesStore, final CmdbuildConfiguration configuration) {
		this.valuesStore = valuesStore;
		this.configuration = configuration;
	}

	@Override
	public UserType getType() {
		UserType type = (UserType) valuesStore.get(AUTH_TYPE_KEY);
		if (type == null) {
			type = UserType.APPLICATION;
			setType(type);
		}
		return type;
	}

	@Override
	public void setType(final UserType type) {
		valuesStore.set(AUTH_TYPE_KEY, type);
	}

	@Override
	public Login getLogin() {
		Login type = (Login) valuesStore.get(LOGIN_KEY);
		if (type == null) {
			type = Login.newInstance(EMPTY);
			setLogin(type);
		}
		return type;
	}

	@Override
	public void setLogin(final Login login) {
		valuesStore.set(LOGIN_KEY, login);
	}

	@Override
	public String getLanguage() {
		String language = (String) valuesStore.get(LANGUAGE_KEY);
		if (language == null) {
			language = configuration.getLanguage();
			setLanguage(language);
		}
		return language;
	}

	@Override
	public void setLanguage(final String language) {
		valuesStore.set(LANGUAGE_KEY, language);
	}

	public ReportFactory getReportFactory() {
		return (ReportFactory) valuesStore.get(REPORTFACTORY_KEY);
	}

	public void setReportFactory(final ReportFactory value) {
		valuesStore.set(REPORTFACTORY_KEY, value);
	}

	public void removeReportFactory() {
		valuesStore.remove(REPORTFACTORY_KEY);
	}

	public Report getNewReport() {
		return (Report) valuesStore.get(NEWREPORT_KEY);
	}

	public void setNewReport(final Report newReport) {
		valuesStore.set(NEWREPORT_KEY, newReport);
	}

	public void removeNewReport() {
		valuesStore.remove(NEWREPORT_KEY);
	}

	public CSVData getCsvData() {
		return (CSVData) valuesStore.get(CSVDATA_KEY);
	}

	public void setCsvData(final CSVData value) {
		valuesStore.set(CSVDATA_KEY, value);
	}
}
