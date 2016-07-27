package org.cmdbuild.logic.translation;

import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.Map;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.logic.setup.SetupLogic;

import com.google.common.collect.Lists;

public class DefaultSetupFacade implements SetupFacade {

	private static final String SEPARATOR = ",";
	private static final String MODULE_NAME = "cmdbuild";
	private static final Object ENABLED_LANGUAGES = "enabled_languages";

	private final SetupLogic setupLogic;
	private final LanguageStore languageStore;

	public DefaultSetupFacade(final SetupLogic setupLogic, final LanguageStore languageStore) {
		this.setupLogic = setupLogic;
		this.languageStore = languageStore;
	}

	@Override
	public boolean isEnabled() {
		return !isEmpty(getEnabledLanguages());
	}

	@Override
	public String getLocalization() {
		return languageStore.getLanguage();
	}

	@Override
	public Iterable<String> getEnabledLanguages() {
		Map<String, String> config;
		String[] enabledLanguagesArray = null;
		String enabledLanguagesConfiguration = EMPTY;
		try {
			config = setupLogic.load(MODULE_NAME);
			enabledLanguagesConfiguration = config.get(ENABLED_LANGUAGES);
			enabledLanguagesConfiguration = enabledLanguagesConfiguration.replaceAll("\\s", "");
		} catch (final Exception e) {
			e.printStackTrace();
		}
		if (isNotBlank(enabledLanguagesConfiguration)) {
			enabledLanguagesArray = enabledLanguagesConfiguration.split(SEPARATOR);
		}
		Iterable<String> enabledLanguages = Lists.newArrayList();
		if (enabledLanguagesArray != null) {
			enabledLanguages = Arrays.asList(enabledLanguagesArray);
		}
		return enabledLanguages;
	}

}
