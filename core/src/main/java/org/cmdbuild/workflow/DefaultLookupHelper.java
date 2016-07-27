package org.cmdbuild.workflow;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.enhydra.shark.api.common.SharkConstants;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class DefaultLookupHelper implements LookupHelper {

	private static final String FLOW_STATUS_LOOKUP = "FlowStatus";

	private static final LookupType FLOW_STATUS = LookupType.newInstance() //
			.withName(FLOW_STATUS_LOOKUP) //
			.build();

	private static final BiMap<String, WSProcessInstanceState> stateByFlowStatusCode;

	static {
		stateByFlowStatusCode = HashBiMap.create();
		stateByFlowStatusCode.put(SharkConstants.STATE_OPEN_RUNNING, WSProcessInstanceState.OPEN);
		stateByFlowStatusCode.put(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED, WSProcessInstanceState.SUSPENDED);
		stateByFlowStatusCode.put(SharkConstants.STATE_CLOSED_COMPLETED, WSProcessInstanceState.COMPLETED);
		stateByFlowStatusCode.put(SharkConstants.STATE_CLOSED_TERMINATED, WSProcessInstanceState.TERMINATED);
		stateByFlowStatusCode.put(SharkConstants.STATE_CLOSED_ABORTED, WSProcessInstanceState.ABORTED);
	}

	private static final Optional<Lookup> MISSING_LOOKUP = Optional.absent();

	private final LookupStore delegate;

	public DefaultLookupHelper(final LookupStore delegate) {
		this.delegate = delegate;
	}

	@Override
	public WSProcessInstanceState stateForLookupCode(final String code) {
		if (code == null) {
			// TODO why not UNSUPPORTED?
			return null;
		}
		final WSProcessInstanceState state = stateByFlowStatusCode.get(code);
		return (state == null) ? WSProcessInstanceState.UNSUPPORTED : state;
	}

	@Override
	public Optional<Lookup> lookupForState(final WSProcessInstanceState state) {
		final String code = stateByFlowStatusCode.inverse().get(state);
		return flowStatusWithCode(code);
	}

	@Override
	public Optional<Lookup> flowStatusWithCode(final String code) {
		for (final Lookup lookup : allLookups()) {
			if (code.equals(lookup.code())) {
				return Optional.of(lookup);
			}
		}
		return MISSING_LOOKUP;
	}

	@Override
	public WSProcessInstanceState stateForLookupId(final Long id) {
		for (final Lookup lookup : allLookups()) {
			if (lookup.getId().equals(id)) {
				final WSProcessInstanceState state = stateForLookupCode(lookup.code());
				return (state == null) ? WSProcessInstanceState.UNSUPPORTED : state;
			}
		}
		return WSProcessInstanceState.UNSUPPORTED;
	}

	@Override
	public Iterable<Lookup> allLookups() {
		return delegate.readAll(FLOW_STATUS);
	}

}
