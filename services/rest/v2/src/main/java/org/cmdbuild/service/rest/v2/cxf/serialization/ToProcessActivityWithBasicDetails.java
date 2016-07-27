package org.cmdbuild.service.rest.v2.cxf.serialization;

import org.cmdbuild.service.rest.v2.model.ProcessActivityWithBasicDetails;

import com.google.common.base.Function;

public abstract class ToProcessActivityWithBasicDetails<F> implements Function<F, ProcessActivityWithBasicDetails> {

	protected ToProcessActivityWithBasicDetails() {
		// usable by subclasses only
	}

}
