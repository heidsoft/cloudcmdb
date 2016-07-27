package org.cmdbuild.service.rest.v1.cxf.service;

import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;

public interface SessionStore {

	boolean has(String id);

	Optional<Session> get(String id);

	void put(Session element);

	void remove(String id);

}
