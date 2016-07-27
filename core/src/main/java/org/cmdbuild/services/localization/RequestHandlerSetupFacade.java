package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.SetupFacade.ForwardingSetupFacade;

public class RequestHandlerSetupFacade extends ForwardingSetupFacade implements RequestHandler {

	private static final ThreadLocal<Boolean> localizedStore = new ThreadLocal<Boolean>();
	private static final ThreadLocal<String> localizationStore = new ThreadLocal<String>();

	private final SetupFacade delegate;

	public RequestHandlerSetupFacade(final SetupFacade delegate) {
		this.delegate = delegate;
	}

	@Override
	protected SetupFacade delegate() {
		return delegate;
	};

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && defaultIfNull(localizedStore.get(), false);
	}

	@Override
	public String getLocalization() {
		return defaultIfBlank(localizationStore.get(), super.getLocalization());
	}

	@Override
	public void setLocalized(final boolean localized) {
		localizedStore.set(localized);
	}

	@Override
	public void setLocalization(final String localization) {
		localizationStore.set(localization);
	}

}
