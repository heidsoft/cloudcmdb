package org.cmdbuild.workflow.xpdl;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.ActivityPerformer;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMActivityWidget;

import com.google.common.base.Function;

public class XpdlActivityWrapper implements CMActivity {

	private static CMValueSet UNAVAILABLE_PROCESS_INSTANCE = new CMValueSet() {

		private final Iterable<Map.Entry<String, Object>> NO_VALUES = emptyList();

		@Override
		public Object get(final String key) {
			return null;
		}

		@Override
		public <T> T get(final String key, final Class<? extends T> requiredType) {
			return null;
		}

		@Override
		public <T> T get(final String key, final Class<? extends T> requiredType, final T defaultValue) {
			return null;
		}

		@Override
		public Iterable<Map.Entry<String, Object>> getValues() {
			return NO_VALUES;
		}

	};

	@Legacy("As in 1.x")
	public static final String ADMIN_START_XA = "AdminStart";

	private final XpdlActivity inner;
	private final XpdlExtendedAttributeVariableFactory variableFactory;
	private final XpdlExtendedAttributeMetadataFactory metadataFactory;
	private final XpdlExtendedAttributeWidgetFactory widgetFactory;

	public XpdlActivityWrapper(final XpdlActivity xpdlActivity,
			final XpdlExtendedAttributeVariableFactory variableFactory,
			final XpdlExtendedAttributeMetadataFactory metadataFactory,
			final XpdlExtendedAttributeWidgetFactory widgetFactory) {
		this.inner = notNull(xpdlActivity, "missing " + XpdlActivity.class);
		this.variableFactory = notNull(variableFactory, "missing " + XpdlExtendedAttributeVariableFactory.class);
		this.metadataFactory = notNull(metadataFactory, "missing " + XpdlExtendedAttributeMetadataFactory.class);
		this.widgetFactory = notNull(widgetFactory, "missing " + XpdlExtendedAttributeWidgetFactory.class);
	}

	@Override
	public List<ActivityPerformer> getPerformers() {
		final List<ActivityPerformer> out = new ArrayList<ActivityPerformer>();
		out.add(getFirstNonAdminPerformer());
		if (isAdminStart()) {
			out.add(ActivityPerformer.newAdminPerformer());
		}
		return out;
	}

	@Legacy("As in 1.x")
	private boolean isAdminStart() {
		return inner.hasExtendedAttributeIgnoreCase(ADMIN_START_XA);
	}

	@Override
	public String getId() {
		return inner.getId();
	}

	@Override
	public String getDescription() {
		return inner.getName();
	}

	@Override
	public String getInstructions() {
		return inner.getDescription();
	}

	@Override
	public ActivityPerformer getFirstNonAdminPerformer() {
		final String performerString = inner.getFirstPerformer();
		if (performerString == null) {
			return ActivityPerformer.newUnknownPerformer();
		}
		if (inner.getProcess().hasRoleParticipant(performerString)) {
			return ActivityPerformer.newRolePerformer(performerString);
		} else {
			return ActivityPerformer.newExpressionPerformer(performerString);
		}
	}

	@Override
	public List<CMActivityVariableToProcess> getVariables() {
		final List<CMActivityVariableToProcess> vars = new ArrayList<CMActivityVariableToProcess>();
		for (final XpdlExtendedAttribute xa : inner.getExtendedAttributes()) {
			final CMActivityVariableToProcess v = variableFactory.createVariable(xa);
			if (v != null) {
				vars.add(v);
			}
		}
		return vars;
	}

	@Override
	public Iterable<CMActivityMetadata> getMetadata() {
		return from(inner.getExtendedAttributes()) //
				.transform(new Function<XpdlExtendedAttribute, CMActivityMetadata>() {

					@Override
					public CMActivityMetadata apply(final XpdlExtendedAttribute input) {
						return metadataFactory.createMetadata(input);
					}

				}) //
				.filter(CMActivityMetadata.class);
	}

	@Override
	public List<CMActivityWidget> getWidgets() {
		return getWidgets(UNAVAILABLE_PROCESS_INSTANCE);
	}

	@Override
	public List<CMActivityWidget> getWidgets(final CMValueSet processInstanceVariables) {
		final List<CMActivityWidget> widgets = new ArrayList<CMActivityWidget>();
		for (final XpdlExtendedAttribute xa : inner.getExtendedAttributes()) {
			final CMActivityWidget w = widgetFactory.createWidget(xa, processInstanceVariables);
			if (w != null) {
				widgets.add(w);
			}
		}
		return widgets;
	}
}
