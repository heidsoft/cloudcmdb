package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static org.cmdbuild.service.rest.v2.model.Models.newEmailTemplate;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.service.rest.v2.EmailTemplates;
import org.cmdbuild.service.rest.v2.model.EmailTemplate;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

import com.google.common.base.Function;

public class CxfEmailTemplates implements EmailTemplates {

	private static final Function<Template, String> LOGIC_TO_STRING = new Function<Template, String>() {

		@Override
		public String apply(final Template input) {
			return input.getName();
		}

	};

	private static final Function<Template, EmailTemplate> LOGIC_TO_REST = new Function<EmailTemplateLogic.Template, EmailTemplate>() {

		@Override
		public EmailTemplate apply(final Template input) {
			return newEmailTemplate() //
					.withId(input.getName()) //
					.withName(input.getName()) //
					.withDescription(input.getDescription()) //
					.withFrom(input.getFrom()) //
					.withTo(input.getTo()) //
					.withCc(input.getCc()) //
					.withBcc(input.getBcc()) //
					.withSubject(input.getSubject()) //
					.withBody(input.getBody()) //
					.withAccount(input.getAccount()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.withDelay(input.getDelay()) //
					.build();
		}

	};

	private final EmailTemplateLogic emailTemplateLogic;

	public CxfEmailTemplates(final EmailTemplateLogic emailTemplateLogic) {
		this.emailTemplateLogic = emailTemplateLogic;
	}

	@Override
	public ResponseMultiple<String> readAll(final Integer limit, final Integer offset) {
		final Iterable<Template> elements = emailTemplateLogic.readAll();
		return newResponseMultiple(String.class) //
				.withElements(from(elements) //
						.skip((offset == null) ? 0 : offset) //
						.limit((limit == null) ? MAX_VALUE : limit) //
						.transform(LOGIC_TO_STRING) //
				) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<EmailTemplate> read(final String id) {
		final Template element = emailTemplateLogic.read(id);
		return newResponseSingle(EmailTemplate.class) //
				.withElement(LOGIC_TO_REST.apply(element)) //
				.build();
	}

}
