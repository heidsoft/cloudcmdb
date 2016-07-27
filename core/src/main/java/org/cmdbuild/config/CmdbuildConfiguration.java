package org.cmdbuild.config;

import java.util.EventListener;
import java.util.Locale;

public interface CmdbuildConfiguration {

	interface ChangeListener extends EventListener {

		void changed();

	}

	void addListener(ChangeListener listener);

	Locale getLocale();

	String getLanguage();

	void setLanguage(String language);

	boolean useLanguagePrompt();

	void setLanguagePrompt(boolean languagePrompt);

	String getStartingClassName();

	void setStartingClass(String startingClass);

	String getDemoModeAdmin();

	void setInstanceName(String instanceName);

	String getInstanceName();

	void setTabsPosition(String instanceName);

	String getTabsPosition();

	int getSessionTimeoutOrDefault();

	boolean getLockCard();

	boolean getLockCardUserVisible();

	long getLockCardTimeOut();

	void setLockCard(boolean lock);

	void setLockCardUserVisible(boolean show);

	void setLockCardTimeOut(long seconds);

	String getEnabledLanguages();

	void setEnabledLanguages(String enabledLanguages);

}
