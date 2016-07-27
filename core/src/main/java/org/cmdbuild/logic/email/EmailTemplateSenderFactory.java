package org.cmdbuild.logic.email;

import javax.activation.DataHandler;

import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logic.Action;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;

import com.google.common.base.Supplier;

public interface EmailTemplateSenderFactory {

	interface Builder extends org.apache.commons.lang3.builder.Builder<EmailTemplateSender> {

		Builder withAccount(Supplier<EmailAccount> account);

		Builder withTemplate(Supplier<Template> template);

		Builder withAttachments(Iterable<Supplier<? extends DataHandler>> attachments);

		Builder withTemplateResolver(TemplateResolver templateResolver);

		Builder withReference(Long reference);

	}

	interface EmailTemplateSender extends Action {

	}

	Builder direct();

	Builder queued();

}
