package org.cmdbuild.listeners;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class RequestListener implements ServletRequestListener {

	@Override
	public void requestInitialized(final ServletRequestEvent sre) {
		final ServletRequest req = sre.getServletRequest();
		if (req instanceof HttpServletRequest) {
			final CMDBContext currentRequestContext = new CMDBContext((HttpServletRequest) req);
			contextStore().set(currentRequestContext);
		}
	}

	@Override
	public void requestDestroyed(final ServletRequestEvent sre) {
		contextStore().remove();
	}

	private ContextStore contextStore() {
		return applicationContext().getBean(ContextStore.class);
	}

}
