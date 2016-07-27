package org.cmdbuild.logic.files;

import java.util.concurrent.TimeUnit;

public interface CacheExpiration {

	long duration();

	TimeUnit unit();

}