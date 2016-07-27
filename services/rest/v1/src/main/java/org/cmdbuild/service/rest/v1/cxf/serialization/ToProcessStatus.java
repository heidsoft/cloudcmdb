package org.cmdbuild.service.rest.v1.cxf.serialization;

import static com.google.common.collect.Maps.newHashMap;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessStatus;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.ABORTED;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.COMPLETED;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.OPEN;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.SUSPENDED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_CLOSED_ABORTED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_CLOSED_COMPLETED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_OPEN_RUNNING;

import java.util.Map;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.v1.model.ProcessStatus;

import com.google.common.base.Function;

public class ToProcessStatus implements Function<Lookup, ProcessStatus> {

	private static Map<String, String> map;

	static {
		map = newHashMap();
		map.put(STATE_OPEN_RUNNING, OPEN);
		map.put(STATE_OPEN_NOT_RUNNING_SUSPENDED, SUSPENDED);
		map.put(STATE_CLOSED_COMPLETED, COMPLETED);
		map.put(STATE_CLOSED_ABORTED, ABORTED);
	}

	@Override
	public ProcessStatus apply(final Lookup input) {
		return newProcessStatus() //
				.withId(input.getId()) //
				// FIXME do it in a better way
				.withValue(map.get(input.code())) //
				.withDescription(input.description()) //
				.build();
	}

}
