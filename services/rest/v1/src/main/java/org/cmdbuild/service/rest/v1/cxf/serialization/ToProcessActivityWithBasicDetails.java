package org.cmdbuild.service.rest.v1.cxf.serialization;

import org.cmdbuild.service.rest.v1.model.ProcessActivityWithBasicDetails;

import com.google.common.base.Function;

public abstract class ToProcessActivityWithBasicDetails<F> implements Function<F, ProcessActivityWithBasicDetails> {

	protected ToProcessActivityWithBasicDetails() {
		// usable by subclasses only
	}

}
