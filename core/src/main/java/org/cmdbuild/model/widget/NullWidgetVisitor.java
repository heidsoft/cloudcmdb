package org.cmdbuild.model.widget;

import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.defaultValues;

public class NullWidgetVisitor extends ForwardingWidgetVisitor {

	private static final NullWidgetVisitor INSTANCE = new NullWidgetVisitor();

	public static NullWidgetVisitor getInstance() {
		return INSTANCE;
	}

	private static final WidgetVisitor DELEGATE = newProxy(WidgetVisitor.class, defaultValues());

	private NullWidgetVisitor() {
		// use factory method
	}

	@Override
	protected WidgetVisitor delegate() {
		return DELEGATE;
	}

}
