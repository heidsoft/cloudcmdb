package org.cmdbuild.listeners;

import com.google.common.base.Optional;

public interface ContextStore {

	Optional<CMDBContext> get();

	void set(CMDBContext value);

	void remove();

}