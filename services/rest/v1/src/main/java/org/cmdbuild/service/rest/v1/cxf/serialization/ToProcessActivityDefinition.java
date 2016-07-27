package org.cmdbuild.service.rest.v1.cxf.serialization;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Notes;
import static org.cmdbuild.service.rest.v1.cxf.serialization.ToAttribute.toAttribute;
import static org.cmdbuild.service.rest.v1.model.Models.newAttributeStatus;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;
import static org.cmdbuild.service.rest.v1.model.Models.newWidget;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.model.widget.ForwardingWidgetVisitor;
import org.cmdbuild.model.widget.Grid;
import org.cmdbuild.model.widget.LinkCards;
import org.cmdbuild.model.widget.ManageRelation;
import org.cmdbuild.model.widget.NullWidgetVisitor;
import org.cmdbuild.model.widget.OpenNote;
import org.cmdbuild.model.widget.WidgetVisitor;
import org.cmdbuild.model.widget.customform.CustomForm;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.service.rest.v1.model.Widget;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Function;

public class ToProcessActivityDefinition implements Function<CMActivity, ProcessActivityWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToProcessActivityDefinition> {

		private boolean writable;

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessActivityDefinition build() {
			validate();
			return new ToProcessActivityDefinition(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withWritableStatus(final boolean writable) {
			this.writable = writable;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final boolean writable;

	private ToProcessActivityDefinition(final Builder builder) {
		this.writable = builder.writable;
	}

	@Override
	public ProcessActivityWithFullDetails apply(final CMActivity input) {
		return newProcessActivityWithFullDetails() //
				.withId(input.getId()) //
				.withWritableStatus(writable) //
				.withDescription(input.getDescription()) //
				.withInstructions(input.getInstructions()) //
				.withAttributes(safeAttributesOf(input)) //
				.withWidgets(safeWidgetsOf(input)) //
				.build();
	}

	private Collection<AttributeStatus> safeAttributesOf(final CMActivity input) {
		try {
			final Collection<AttributeStatus> attributes = newArrayList();
			for (final CMActivityVariableToProcess element : input.getVariables()) {
				attributes.add(toAttribute(Long.valueOf(attributes.size())).apply(element));
			}
			for (final org.cmdbuild.model.widget.Widget widget : from(input.getWidgets()) //
					.filter(org.cmdbuild.model.widget.Widget.class)) {
				widget.accept(new ForwardingWidgetVisitor() {

					private final WidgetVisitor delegate = NullWidgetVisitor.getInstance();

					@Override
					protected WidgetVisitor delegate() {
						return delegate;
					}

					@Override
					public void visit(final OpenNote widget) {
						attributes.add(newAttributeStatus() //
								.withId(Notes.getDBName()) //
								.withWritable(true) //
								.withMandatory(false) //
								.withIndex(Long.valueOf(attributes.size())) //
								.build());
					}

				});
			}
			return attributes;
		} catch (final CMWorkflowException e) {
			throw new RuntimeException(e);
		}
	}

	private Iterable<Widget> safeWidgetsOf(final CMActivity input) {
		try {
			return from(input.getWidgets()) //
					.filter(org.cmdbuild.model.widget.Widget.class) //
					.transform(new Function<org.cmdbuild.model.widget.Widget, Widget>() {

						@Override
						public Widget apply(final org.cmdbuild.model.widget.Widget input) {
							/*
							 * TODO do in a better way
							 */
							final ObjectMapper objectMapper = new ObjectMapper();
							@SuppressWarnings("unchecked")
							final Map<String, Object> objectAsMap = objectMapper.convertValue(input, Map.class);
							return newWidget() //
									.withId(input.getIdentifier()) //
									.withType(input.getType()) //
									.withActive(input.isActive()) //
									.withRequired(new ForwardingWidgetVisitor() {

										private final WidgetVisitor delegate = NullWidgetVisitor.getInstance();
										private final org.cmdbuild.model.widget.Widget widget = input;
										private boolean required;

										@Override
										protected WidgetVisitor delegate() {
											return delegate;
										}

										public boolean isRequired() {
											required = false;
											widget.accept(this);
											return required;
										}

										@Override
										public void visit(final CustomForm widget) {
											required = widget.isRequired();
										}

										@Override
										public void visit(final Grid widget) {
											required = widget.isRequired();
										}

										@Override
										public void visit(final LinkCards widget) {
											required = widget.isRequired();
										}

										@Override
										public void visit(final ManageRelation widget) {
											required = widget.isRequired();
										}

									}.isRequired()) //
									.withLabel(input.getLabel()) //
									.withData(newValues() //
											.withValues(objectAsMap) //
											.build()) //
									.build();
						}

					});
		} catch (final CMWorkflowException e) {
			throw new RuntimeException(e);
		}
	}
}
