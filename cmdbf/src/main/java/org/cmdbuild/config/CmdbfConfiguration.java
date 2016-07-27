package org.cmdbuild.config;

public interface CmdbfConfiguration {
	String getMdrId();

	void setMdrId(String mdrId);

	String getSchemaLocation();

	void setSchemaLocation(String schemaLocation);

	String getReconciliationRules();

	void setReconciliationRules(String reconciliationRules);
}
