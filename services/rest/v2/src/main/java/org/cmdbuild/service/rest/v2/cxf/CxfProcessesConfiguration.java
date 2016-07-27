package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import org.cmdbuild.service.rest.v2.ProcessesConfiguration;
import org.cmdbuild.service.rest.v2.model.ProcessStatus;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

public class CxfProcessesConfiguration implements ProcessesConfiguration {

	private final ProcessStatusHelper processStatusHelper;

	public CxfProcessesConfiguration(final ProcessStatusHelper processStatusHelper) {
		this.processStatusHelper = processStatusHelper;
	}

	@Override
	public ResponseMultiple<ProcessStatus> readStatuses() {
		final Iterable<ProcessStatus> elements = processStatusHelper.allValues();
		return newResponseMultiple(ProcessStatus.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

}
