package org.cmdbuild.service.rest.v2.cxf;

import org.cmdbuild.service.rest.v2.model.ProcessStatus;

import com.google.common.base.Optional;

public interface ProcessStatusHelper {

	Iterable<ProcessStatus> allValues();

	Optional<ProcessStatus> defaultValue();

}
