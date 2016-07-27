package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.type.ReferenceType;

public interface WorkflowApi extends FluentApi, SchemaApi, MailApi {

	Impersonate impersonate();

	Private soap();

	ReferenceType referenceTypeFrom(Card card);

	ReferenceType referenceTypeFrom(CardDescriptor cardDescriptor);

	ReferenceType referenceTypeFrom(Object idAsObject);

	CardDescriptor cardDescriptorFrom(ReferenceType referenceType);

	Card cardFrom(ReferenceType referenceType);

}
