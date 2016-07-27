package org.cmdbuild.workflow;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

import com.google.common.base.Optional;

public interface LookupHelper {

	WSProcessInstanceState stateForLookupCode(String code);

	Optional<Lookup> lookupForState(WSProcessInstanceState state);

	Optional<Lookup> flowStatusWithCode(String code);

	WSProcessInstanceState stateForLookupId(Long id);

	Iterable<Lookup> allLookups();

}