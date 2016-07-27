package org.cmdbuild.services.meta;

import net.jcip.annotations.NotThreadSafe;

// FIXME: it's not enough to synchronize the update
@NotThreadSafe
public class MetadataService {

	private static final String RUNTIME_PREFIX = "runtime";
	public static final String RUNTIME_PRIVILEGES_KEY = RUNTIME_PREFIX + ".privileges";
	public static final String RUNTIME_USERNAME_KEY = RUNTIME_PREFIX + ".username";
	public static final String RUNTIME_DEFAULTGROUPNAME_KEY = RUNTIME_PREFIX + ".groupname";
	public static final String RUNTIME_PROCESS_ISSTOPPABLE = RUNTIME_PREFIX + ".processstoppable";

	public static final String SYSTEM_PREFIX = "system";
	public static final String SYSTEM_TEMPLATE_PREFIX = SYSTEM_PREFIX + ".template";

}
