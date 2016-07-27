package org.cmdbuild.logic.data;

import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.defaultValues;

public class DummyLockLogic extends ForwardingLockLogic {

	private static final LockLogic DELEGATE = newProxy(LockLogic.class, defaultValues());

	@Override
	protected LockLogic delegate() {
		return DELEGATE;
	}

}
