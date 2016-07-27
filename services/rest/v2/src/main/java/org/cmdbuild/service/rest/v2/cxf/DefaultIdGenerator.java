package org.cmdbuild.service.rest.v2.cxf;

import static java.lang.Long.MAX_VALUE;
import static org.apache.commons.lang3.RandomUtils.nextLong;

public class DefaultIdGenerator implements IdGenerator {

	@Override
	public Long generate() {
		return -nextLong(1, MAX_VALUE);
	}

	@Override
	public boolean isGenerated(final Long id) {
		return id < 0;
	}

}
