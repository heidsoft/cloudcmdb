package unit.cxf;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.service.rest.v2.model.Models.newEmail;
import static org.cmdbuild.service.rest.v2.model.Models.newLongId;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.NoSuchElementException;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.logic.email.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstanceEmails;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.cxf.IdGenerator;
import org.cmdbuild.service.rest.v2.model.Email;
import org.cmdbuild.service.rest.v2.model.LongId;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CxfProcessInstanceEmailsTest {

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;
	private EmailLogic emailLogic;
	private IdGenerator idGenerator;

	private CxfProcessInstanceEmails underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		emailLogic = mock(EmailLogic.class);
		idGenerator = mock(IdGenerator.class);
		underTest = new CxfProcessInstanceEmails(errorHandler, workflowLogic, emailLogic, idGenerator);
	}

	@Test(expected = WebApplicationException.class)
	public void createFailsWhenProcessIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("dummy"));

		// when
		underTest.create("dummy", 12L, newEmail() //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test(expected = WebApplicationException.class)
	public void createFailsWhenInstanceIsNotFound() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(eq("dummy"));
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("dummy"), eq(12L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(12L));

		// when
		underTest.create("dummy", 12L, newEmail() //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test(expected = RuntimeException.class)
	public void createFailsWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).create(eq(EmailImpl.newInstance() //
						.withReference(12L) //
						.withFromAddress("from@example.com") //
						.withToAddresses("to@example.com") //
						.withSubject("subject") //
						.withContent("body") //
						.build()));

		// when
		underTest.create("dummy", 12L, newEmail() //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test
	public void createReturnsIdReturnedFromLogic() throws Exception {
		// given
		doReturn(42L) //
				.when(emailLogic).create(any(EmailLogic.Email.class));

		// when
		final ResponseSingle<Long> response = underTest.create("dummy", 12L, newEmail() //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withCc("cc@example.com,another_cc@gmail.com") //
				.withBcc("bcc@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.withStatus("draft") //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build());

		// then
		assertThat(response.getElement(), equalTo(42L));

		verify(emailLogic).create(eq(EmailImpl.newInstance() //
				.withFromAddress("from@example.com") //
				.withToAddresses("to@example.com") //
				.withCcAddresses("cc@example.com,another_cc@gmail.com") //
				.withBccAddresses("bcc@example.com") //
				.withSubject("subject") //
				.withContent("body") //
				.withStatus(draft()) //
				.withReference(12L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(false) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void readAllFailsWhenProcessIsNotFound() throws Exception {
		// given
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("dummy"));

		// when
		underTest.readAll("dummy", 12L, 34, 56);
	}

	@Ignore("ignored until a better solution will be found")
	@Test(expected = WebApplicationException.class)
	public void readAllFailsWhenInstanceIsNotFound() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(eq("dummy"));
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("dummy"), eq(12L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(12L));

		// when
		underTest.readAll("dummy", 12L, 34, 56);
	}

	@Test(expected = RuntimeException.class)
	public void readAllFailsWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).readAll(isNull(Long.class));

		// when
		underTest.readAll("dummy", 12L, 34, 56);
	}

	@Test
	public void readAllCanLimitOutput() throws Exception {
		// given
		final EmailLogic.Email email_1 = EmailImpl.newInstance() //
				.withId(1L) //
				.build();
		final EmailLogic.Email email_2 = EmailImpl.newInstance() //
				.withId(2L) //
				.build();
		final EmailLogic.Email email_3 = EmailImpl.newInstance() //
				.withId(3L) //
				.build();
		final EmailLogic.Email email_4 = EmailImpl.newInstance() //
				.withId(3L) //
				.build();
		doReturn(asList(email_1, email_2, email_3, email_4)) //
				.when(emailLogic).readAll(any(Long.class));

		// when
		final ResponseMultiple<LongId> response = underTest.readAll("dummy", 12L, 1, 2);

		// then
		assertThat(newArrayList(response.getElements()), equalTo(asList(newLongId() //
				.withId(3L) //
				.build())));
		assertThat(response.getMetadata().getTotal(), equalTo(4L));

		verify(emailLogic).readAll(eq(12L));
	}

	@Test(expected = WebApplicationException.class)
	public void readFailsWhenProcessIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("dummy"));

		// when
		underTest.read("dummy", 12L, 34L);
	}

	@Ignore("ignored until a better solution will be found")
	@Test(expected = WebApplicationException.class)
	public void readFailsWhenInstanceIsNotFound() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(eq("dummy"));
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("dummy"), eq(12L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(12L));

		// when
		underTest.read("dummy", 12L, 34L);
	}

	@Test(expected = RuntimeException.class)
	public void readFailsWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).read(eq(EmailImpl.newInstance() //
						.withId(34L) //
						.build()));

		// when
		underTest.read("dummy", 12L, 34L);
	}

	@Test
	public void readReturnsIdReturnedFromLogic() throws Exception {
		// given
		final EmailLogic.Email read = EmailImpl.newInstance() //
				.withId(56L) //
				.withFromAddress("from@example.com") //
				.withToAddresses("to@example.com") //
				.withCcAddresses("cc@example.com,another_cc@gmail.com") //
				.withBccAddresses("bcc@example.com") //
				.withSubject("subject") //
				.withContent("body") //
				.withStatus(draft()) //
				.withReference(34L) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(true) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build();
		doReturn(read) //
				.when(emailLogic).read(any(EmailLogic.Email.class));

		// when
		final ResponseSingle<Email> response = underTest.read("dummy", 12L, 34L);

		// then
		assertThat(response.getElement(), equalTo(newEmail() //
				.withId(56L) //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withCc("cc@example.com,another_cc@gmail.com") //
				.withBcc("bcc@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.withStatus("draft") //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build()));

		verify(emailLogic).read(eq(EmailImpl.newInstance() //
				.withId(34L) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void updateFailsWhenProcessIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("dummy"));

		// when
		underTest.update("dummy", 12L, 34L, newEmail() //
				.withId(56L) //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test(expected = WebApplicationException.class)
	public void updateFailsWhenInstanceIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("dummy"));

		// when
		underTest.update("dummy", 12L, 34L, newEmail() //
				.withId(56L) //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test(expected = RuntimeException.class)
	public void updateFailsWhenLogicThrowsException() throws Exception {
		// given
		final Throwable e = new RuntimeException();
		doThrow(e) //
				.when(emailLogic).update(eq(EmailImpl.newInstance() //
						.withId(34L) //
						.withReference(12L) //
						.withFromAddress("from@example.com") //
						.withToAddresses("to@example.com") //
						.withSubject("subject") //
						.withContent("body") //
						.build()));

		// when
		underTest.update("dummy", 12L, 34L, newEmail() //
				.withId(56L) //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.build());
	}

	@Test
	public void updateReturnsIdReturnedFromLogic() throws Exception {
		// when
		underTest.update("dummy", 12L, 34L, newEmail() //
				.withId(56L) //
				.withFrom("from@example.com") //
				.withTo("to@example.com") //
				.withCc("cc@example.com,another_cc@gmail.com") //
				.withBcc("bcc@example.com") //
				.withSubject("subject") //
				.withBody("body") //
				.withStatus("draft") //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build());

		// then
		verify(emailLogic).update(eq(EmailImpl.newInstance() //
				.withId(34L) //
				.withReference(12L) //
				.withFromAddress("from@example.com") //
				.withToAddresses("to@example.com") //
				.withCcAddresses("cc@example.com,another_cc@gmail.com") //
				.withBccAddresses("bcc@example.com") //
				.withSubject("subject") //
				.withContent("body") //
				.withStatus(draft()) //
				.withNotifyWith("foo") //
				.withNoSubjectPrefix(true) //
				.withAccount("bar") //
				.withTemporary(false) //
				.withTemplate("baz") //
				.withKeepSynchronization(true) //
				.withPromptSynchronization(true) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void deleteFailsWhenProcessIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("dummy"));

		// when
		underTest.delete("dummy", 12L, 34L);
	}

	@Ignore("ignored until a better solution will be found")
	@Test(expected = RuntimeException.class)
	public void deleteFailsWhenInstanceIsNotFound() throws Exception {
		// given
		final UserProcessClass targetClass = mock(UserProcessClass.class);
		doReturn(targetClass) //
				.when(workflowLogic).findProcessClass(eq("dummy"));
		doThrow(new NoSuchElementException()) //
				.when(workflowLogic).getProcessInstance(eq("dummy"), eq(12L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).cardNotFound(eq(12L));

		// when
		underTest.delete("dummy", 12L, 34L);
	}

	@Test
	public void deleteReturnsIdReturnedFromLogic() throws Exception {
		// when
		underTest.delete("dummy", 12L, 34L);

		// then
		verify(emailLogic).delete(eq(EmailImpl.newInstance() //
				.withId(34L) //
				.build()));
	}

}
