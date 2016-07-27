package unit.cxf;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v2.model.Models.newEmailTemplate;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.service.rest.v2.cxf.CxfEmailTemplates;
import org.cmdbuild.service.rest.v2.model.EmailTemplate;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Test;

public class CxfEmailTemplatesTest {

	private static class TestDoubleTemplate implements Template {

		private static final Map<String, String> NO_VARIABLES = Collections.emptyMap();

		private static class Builder implements org.apache.commons.lang3.builder.Builder<TestDoubleTemplate> {

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
			public TestDoubleTemplate build() {
				return new TestDoubleTemplate(this);
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

		private TestDoubleTemplate(final Builder builder) {
			this.id = builder.id;
			this.name = builder.name;
			this.description = builder.description;
			this.from = builder.from;
			this.to = builder.to;
			this.cc = builder.cc;
			this.bcc = builder.bcc;
			this.subject = builder.subject;
			this.body = builder.body;
			this.account = builder.account;
			this.keepSynchronization = builder.keepSynchronization;
			this.promptSynchronization = builder.promptSynchronization;
			this.delay = builder.delay;
			this.variables = builder.variables;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getFrom() {
			return from;
		}

		@Override
		public String getTo() {
			return to;
		}

		@Override
		public String getCc() {
			return cc;
		}

		@Override
		public String getBcc() {
			return bcc;
		}

		@Override
		public String getSubject() {
			return subject;
		}

		@Override
		public String getBody() {
			return body;
		}

		@Override
		public String getAccount() {
			return account;
		}

		@Override
		public boolean isKeepSynchronization() {
			return keepSynchronization;
		}

		@Override
		public boolean isPromptSynchronization() {
			return promptSynchronization;
		}

		@Override
		public long getDelay() {
			return delay;
		}

		@Override
		public Map<String, String> getVariables() {
			return defaultIfNull(variables, NO_VARIABLES);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Template)) {
				return false;
			}
			final Template other = Template.class.cast(obj);
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

	private EmailTemplateLogic emailTemplateLogic;

	private CxfEmailTemplates underTest;

	@Before
	public void setUp() throws Exception {
		emailTemplateLogic = mock(EmailTemplateLogic.class);
		underTest = new CxfEmailTemplates(emailTemplateLogic);
	}

	@Test(expected = RuntimeException.class)
	public void readAllFailsWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailTemplateLogic).readAll();

		// when
		underTest.readAll(12, 34);
	}

	@Test
	public void readAllCanLimitOutput() throws Exception {
		// given
		final Template email_1 = TestDoubleTemplate.newInstance() //
				.withName("1") //
				.build();
		final Template email_2 = TestDoubleTemplate.newInstance() //
				.withName("2") //
				.build();
		final Template email_3 = TestDoubleTemplate.newInstance() //
				.withName("3") //
				.build();
		final Template email_4 = TestDoubleTemplate.newInstance() //
				.withName("4") //
				.build();
		doReturn(asList(email_1, email_2, email_3, email_4)) //
				.when(emailTemplateLogic).readAll();

		// when
		final ResponseMultiple<String> response = underTest.readAll(1, 2);

		// then
		assertThat(newArrayList(response.getElements()), equalTo(asList(email_3.getName())));
		assertThat(response.getMetadata().getTotal(), equalTo(4L));

		verify(emailTemplateLogic).readAll();
	}

	@Test(expected = RuntimeException.class)
	public void readFailsWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailTemplateLogic).read(eq("dummy"));

		// when
		underTest.read("dummy");
	}

	@Test
	public void readReturnsIdReturnedFromLogic() throws Exception {
		// given
		final Template read = TestDoubleTemplate.newInstance() //
				.withId(42L) //
				.withName("dummy") //
				.withDescription("this is dummy") //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withCc("cc@example.com,another_cc@gmail.com") //
				.withBcc("bcc@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.withAccount("bar") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.withDelay(123L) //
				.build();
		doReturn(read) //
				.when(emailTemplateLogic).read(anyString());

		// when
		final ResponseSingle<EmailTemplate> response = underTest.read("dummy");

		// then
		assertThat(response.getElement(), equalTo(newEmailTemplate() //
				.withId("dummy") //
				.withName("dummy") //
				.withDescription("this is dummy") //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withCc("cc@example.com,another_cc@gmail.com") //
				.withBcc("bcc@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.withAccount("bar") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.withDelay(123L) //
				.build()));

		verify(emailTemplateLogic).read(eq("dummy"));
	}

}
