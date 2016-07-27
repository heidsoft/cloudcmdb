package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.data.store.lookup.Predicates.defaultLookup;

import org.cmdbuild.service.rest.v1.cxf.serialization.ToProcessStatus;
import org.cmdbuild.service.rest.v1.model.ProcessStatus;
import org.cmdbuild.workflow.LookupHelper;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class DefaultProcessStatusHelper implements ProcessStatusHelper {

	private static final ToProcessStatus TO_PROCESS_STATUS = new ToProcessStatus();

	private static final Predicate<ProcessStatus> VALID_STATUSES = new Predicate<ProcessStatus>() {

		@Override
		public boolean apply(final ProcessStatus input) {
			return (input.getValue() != null);
		}

	};

	private final LookupHelper lookupHelper;

	public DefaultProcessStatusHelper(final LookupHelper lookupHelper) {
		this.lookupHelper = lookupHelper;
	}

	@Override
	public Iterable<ProcessStatus> allValues() {
		return from(lookupHelper.allLookups()) //
				.transform(TO_PROCESS_STATUS) //
				.filter(VALID_STATUSES);
	}

	@Override
	public Optional<ProcessStatus> defaultValue() {
		return from(lookupHelper.allLookups()) //
				.filter(defaultLookup()) //
				.transform(TO_PROCESS_STATUS) //
				.filter(VALID_STATUSES) //
				.first();
	}

}
