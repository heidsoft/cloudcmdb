package org.cmdbuild.servlets.json.email;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.servlets.json.CommunicationConstants.BCC;
import static org.cmdbuild.servlets.json.CommunicationConstants.BODY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CC;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DELAY;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.FROM;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.KEEP_SYNCHRONIZATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROMPT_SYNCHRONIZATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPLATES;
import static org.cmdbuild.servlets.json.CommunicationConstants.TO;
import static org.cmdbuild.servlets.json.CommunicationConstants.VARIABLES;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class Template extends JSONBaseWithSpringContext {

	private static class JsonTemplate implements EmailTemplateLogic.Template {

		private static final Map<String, String> NO_VARIABLES = Collections.emptyMap();

		private static class Builder implements org.apache.commons.lang3.builder.Builder<JsonTemplate> {

			private Long id;
			private String name;
			private String description;
			private String from;
			private String to;
			private String cc;
			private String bcc;
			private String subject;
			private String body;
			private String account;
			private boolean keepSynchronization;
			private boolean promptSynchronization;
			private long delay;
			private Map<String, String> variables;

			private Builder() {
				// use factory method
			}

			@Override
			public JsonTemplate build() {
				return new JsonTemplate(this);
			}

			public Builder withId(final Long id) {
				this.id = id;
				return this;
			}

			public Builder withDescription(final String description) {
				this.description = description;
				return this;
			}

			public Builder withName(final String name) {
				this.name = name;
				return this;
			}

			public Builder withFrom(final String from) {
				this.from = from;
				return this;
			}

			public Builder withTo(final String to) {
				this.to = to;
				return this;
			}

			public Builder withCc(final String cc) {
				this.cc = cc;
				return this;
			}

			public Builder withBcc(final String bcc) {
				this.bcc = bcc;
				return this;
			}

			public Builder withSubject(final String subject) {
				this.subject = subject;
				return this;
			}

			public Builder withBody(final String body) {
				this.body = body;
				return this;
			}

			public Builder withAccount(final String account) {
				this.account = account;
				return this;
			}

			public Builder withKeepSynchronization(final boolean keepSynchronization) {
				this.keepSynchronization = keepSynchronization;
				return this;
			}

			public Builder withPromptSynchronization(final boolean promptSynchronization) {
				this.promptSynchronization = promptSynchronization;
				return this;
			}

			public Builder withDelay(final long delay) {
				this.delay = delay;
				return this;
			}

			public Builder withVariables(final Map<String, String> variables) {
				this.variables = variables;
				return this;
			}

			@Override
			public String toString() {
				return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final Long id;
		private final String name;
		private final String description;
		private final String from;
		private final String to;
		private final String cc;
		private final String bcc;
		private final String subject;
		private final String body;
		private final String account;
		private final boolean keepSynchronization;
		private final boolean promptSynchronization;
		private final long delay;
		private final Map<String, String> variables;

		private JsonTemplate(final Builder builder) {
			this.id = builder.id;
			this.name = builder.name;
			this.description = builder.description;
			this.from = builder.from;
			this.to = builder.to;
			this.cc = builder.cc;
			this.bcc = builder.bcc;
			this.subject = builder.subject;
			this.body = builder.body;
			this.variables = builder.variables;
			this.account = builder.account;
			this.keepSynchronization = builder.keepSynchronization;
			this.promptSynchronization = builder.promptSynchronization;
			this.delay = builder.delay;
		}

		@Override
		@JsonProperty(ID)
		public Long getId() {
			return id;
		}

		@Override
		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		@Override
		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return description;
		}

		@Override
		@JsonProperty(FROM)
		public String getFrom() {
			return from;
		}

		@Override
		@JsonProperty(TO)
		public String getTo() {
			return to;
		}

		@Override
		@JsonProperty(CC)
		public String getCc() {
			return cc;
		}

		@Override
		@JsonProperty(BCC)
		public String getBcc() {
			return bcc;
		}

		@Override
		@JsonProperty(SUBJECT)
		public String getSubject() {
			return subject;
		}

		@Override
		@JsonProperty(BODY)
		public String getBody() {
			return body;
		}

		@Override
		@JsonProperty(DEFAULT_ACCOUNT)
		public String getAccount() {
			return account;
		}

		@Override
		@JsonProperty(KEEP_SYNCHRONIZATION)
		public boolean isKeepSynchronization() {
			return keepSynchronization;
		}

		@Override
		@JsonProperty(PROMPT_SYNCHRONIZATION)
		public boolean isPromptSynchronization() {
			return promptSynchronization;
		}

		@Override
		@JsonProperty(DELAY)
		public long getDelay() {
			return delay;
		}

		@Override
		@JsonProperty(VARIABLES)
		public Map<String, String> getVariables() {
			return defaultIfNull(variables, NO_VARIABLES);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof EmailTemplateLogic.Template)) {
				return false;
			}
			final EmailTemplateLogic.Template other = EmailTemplateLogic.Template.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getName(), other.getName()) //
					.append(this.getDescription(), other.getDescription()) //
					.append(this.getFrom(), other.getFrom()) //
					.append(this.getTo(), other.getTo()) //
					.append(this.getCc(), other.getCc()) //
					.append(this.getBcc(), other.getBcc()) //
					.append(this.getSubject(), other.getSubject()) //
					.append(this.getBody(), other.getBody()) //
					.append(this.getAccount(), other.getAccount()) //
					.append(this.isKeepSynchronization(), other.isKeepSynchronization()) //
					.append(this.isPromptSynchronization(), other.isPromptSynchronization()) //
					.append(this.getDelay(), other.getDelay()) //
					.append(this.getVariables(), other.getVariables()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(id) //
					.append(name) //
					.append(description) //
					.append(from) //
					.append(to) //
					.append(cc) //
					.append(bcc) //
					.append(subject) //
					.append(body) //
					.append(account) //
					.append(keepSynchronization) //
					.append(promptSynchronization) //
					.append(delay) //
					.append(variables) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class JsonTemplates {

		private static class Builder implements org.apache.commons.lang3.builder.Builder<JsonTemplates> {

			private List<? super JsonTemplate> elements;

			private Builder() {
				// use factory method
			}

			@Override
			public JsonTemplates build() {
				return new JsonTemplates(this);
			}

			public Builder withElements(final Iterable<? extends JsonTemplate> elements) {
				this.elements = Lists.newArrayList(elements);
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final List<? super JsonTemplate> elements;

		private JsonTemplates(final Builder builder) {
			this.elements = builder.elements;
		}

		@JsonProperty(ELEMENTS)
		public List<? super JsonTemplate> getElements() {
			return elements;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof JsonTemplates)) {
				return false;
			}
			final JsonTemplates other = JsonTemplates.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.elements, other.elements) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(elements) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static Function<EmailTemplateLogic.Template, JsonTemplate> TEMPLATE_TO_JSON_TEMPLATE = new Function<EmailTemplateLogic.Template, JsonTemplate>() {

		@Override
		public JsonTemplate apply(final EmailTemplateLogic.Template input) {
			return JsonTemplate.newInstance() //
					.withId(input.getId()) //
					.withName(input.getName()) //
					.withDescription(input.getDescription()) //
					.withFrom(input.getFrom()) //
					.withTo(input.getTo()) //
					.withCc(input.getCc()) //
					.withBcc(input.getBcc()) //
					.withSubject(input.getSubject()) //
					.withBody(input.getBody()) //
					.withVariables(input.getVariables()) //
					.withAccount(input.getAccount()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.withDelay(input.getDelay()) //
					.build();
		}

	};

	@JSONExported
	@Admin
	public JsonResponse create( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(FROM) final String from, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = VARIABLES, required = false) final JSONObject jsonVariables, //
			@Parameter(value = DEFAULT_ACCOUNT, required = false) final String accountName, //
			@Parameter(value = KEEP_SYNCHRONIZATION, required = false) final boolean keepSynchronization, //
			@Parameter(value = PROMPT_SYNCHRONIZATION, required = false) final boolean promptSynchronization, //
			@Parameter(value = DELAY, required = false) final long delay //
	) {
		final Long id = emailTemplateLogic().create(JsonTemplate.newInstance() //
				.withName(name) //
				.withDescription(description) //
				.withFrom(from) //
				.withTo(to) //
				.withCc(cc) //
				.withBcc(bcc) //
				.withSubject(subject) //
				.withBody(body) //
				.withVariables(toMap(jsonVariables)) //
				.withAccount(accountName) //
				.withKeepSynchronization(keepSynchronization) //
				.withPromptSynchronization(promptSynchronization) //
				.withDelay(delay) //
				.build());
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse readAll( //
			@Parameter(value = TEMPLATES, required = false) final JSONArray templates //
	) {
		final Iterable<EmailTemplateLogic.Template> elements = from(emailTemplateLogic().readAll()) //
				.filter(new Predicate<EmailTemplateLogic.Template>() {

					private final Collection<String> names = from(toIterable(templates)).toList();

					@Override
					public boolean apply(final EmailTemplateLogic.Template input) {
						return names.isEmpty() || names.contains(input.getName());
					}

				});
		return JsonResponse.success(JsonTemplates.newInstance() //
				.withElements(from(elements) //
						.transform(TEMPLATE_TO_JSON_TEMPLATE)) //
				.build());
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(NAME) final String name //
	) {
		final EmailTemplateLogic.Template element = emailTemplateLogic().read(name);
		return JsonResponse.success(TEMPLATE_TO_JSON_TEMPLATE.apply(element));
	}

	@JSONExported
	@Admin
	public void update( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(FROM) final String from, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = VARIABLES, required = false) final JSONObject jsonVariables, //
			@Parameter(value = DEFAULT_ACCOUNT, required = false) final String accountName, //
			@Parameter(value = KEEP_SYNCHRONIZATION, required = false) final boolean keepSynchronization, //
			@Parameter(value = PROMPT_SYNCHRONIZATION, required = false) final boolean promptSynchronization, //
			@Parameter(value = DELAY, required = false) final long delay //
	) {
		emailTemplateLogic().update(JsonTemplate.newInstance() //
				.withName(name) //
				.withDescription(description) //
				.withFrom(from) //
				.withTo(to) //
				.withCc(cc) //
				.withBcc(bcc) //
				.withSubject(subject) //
				.withBody(body) //
				.withVariables(toMap(jsonVariables)) //
				.withAccount(accountName) //
				.withKeepSynchronization(keepSynchronization) //
				.withPromptSynchronization(promptSynchronization) //
				.withDelay(delay) //
				.build());
	}

	@JSONExported
	@Admin
	public void delete( //
			@Parameter(NAME) final String name //
	) {
		emailTemplateLogic().delete(name);
	}

}
