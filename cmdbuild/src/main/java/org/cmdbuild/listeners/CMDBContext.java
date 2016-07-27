package org.cmdbuild.listeners;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.exception.CMDBException;

import com.google.common.collect.Lists;

public class CMDBContext {

	private final HttpServletRequest request;
	private final List<CMDBException> warnings = Lists.newLinkedList();

	public CMDBContext(final HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public List<CMDBException> getWarnings() {
		return warnings;
	}

	public void pushWarning(final CMDBException e) {
		warnings.add(e);
	}

}