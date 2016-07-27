package org.cmdbuild.service.rest.v2.cxf.serialization;

import static org.cmdbuild.service.rest.v2.model.Models.newAttributeStatus;

import org.cmdbuild.service.rest.v2.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

import com.google.common.base.Function;

public class ToAttribute implements Function<CMActivityVariableToProcess, AttributeStatus> {

	public static ToAttribute toAttribute(final Long index) {
		return new ToAttribute(index);
	}

	private final Long index;

	private ToAttribute(final Long index) {
		this.index = index;
	}

	@Override
	public AttributeStatus apply(final CMActivityVariableToProcess input) {
		return newAttributeStatus() //
				.withId(input.getName()) //
				.withWritable(input.isWritable()) //
				.withMandatory(input.isMandatory()) //
				.withIndex(index) //
				.build();
	}

}