package org.cmdbuild.logic.data.access.lock;

import com.google.common.base.Optional;

public interface Lockable {

	Optional<Lockable> parent();

}