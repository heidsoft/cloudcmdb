package org.cmdbuild.logic.translation;

import com.google.common.collect.ForwardingObject;

public interface SetupFacade {

	abstract class ForwardingSetupFacade extends ForwardingObject implements SetupFacade {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingSetupFacade() {
		}

		@Override
		protected abstract SetupFacade delegate();

		@Override
		public boolean isEnabled() {
			return delegate().isEnabled();
		}

		@Override
		public String getLocalization() {
			return delegate().getLocalization();
		}

		@Override
		public Iterable<String> getEnabledLanguages() {
			return delegate().getEnabledLanguages();
		}

	}

	boolean isEnabled();

	Iterable<String> getEnabledLanguages();

	String getLocalization();

}
