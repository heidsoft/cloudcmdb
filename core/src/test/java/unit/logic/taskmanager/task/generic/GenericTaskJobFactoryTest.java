package unit.logic.taskmanager.task.generic;

import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.elementsEqual;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;

import javax.activation.DataHandler;

import org.bimserver.utils.FileDataSource;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.report.ReportLogic.Extension;
import org.cmdbuild.logic.report.ReportLogic.Report;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTaskJobFactory;
import org.cmdbuild.services.template.engine.DatabaseEngine;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Supplier;

public class GenericTaskJobFactoryTest {

	@ClassRule
	public static TemporaryFolder tmp = new TemporaryFolder();

	private EmailAccountFacade emailAccountFacade;
	private EmailTemplateLogic emailTemplateLogic;
	private ReportLogic reportLogic;
	private CMDataView dataView;
	private DatabaseEngine databaseEngine;
	private EmailTemplateSenderFactory emailTemplateSenderFactory;
	private GenericTaskJobFactory underTest;

	@Before
	public void setUp() {
		emailAccountFacade = mock(EmailAccountFacade.class);
		emailTemplateLogic = mock(EmailTemplateLogic.class);
		reportLogic = mock(ReportLogic.class);
		dataView = mock(CMDataView.class);
		databaseEngine = mock(DatabaseEngine.class);
		emailTemplateSenderFactory = mock(EmailTemplateSenderFactory.class);
		underTest = new GenericTaskJobFactory(emailAccountFacade, emailTemplateLogic, reportLogic, dataView,
				databaseEngine, emailTemplateSenderFactory);
	}

	@Test
	public void jobDoesNothingWhenEmailIsNotActive() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(false) //
				.build();

		// when
		underTest.create(task, true).execute();

		// then
		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, reportLogic, dataView, databaseEngine,
				emailTemplateSenderFactory);
	}

	@Test
	public void jobCanBeCreatedWithNoErrorsEvenIfAllParametersAreMissing() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.build();

		// when
		underTest.create(task, true);

		// then
		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, reportLogic, dataView, databaseEngine,
				emailTemplateSenderFactory);
	}

	@Test
	public void jobDelegatesToComponentTheSendOfTheMail() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.build();
		final EmailTemplateSenderFactory.Builder queue = mock(EmailTemplateSenderFactory.Builder.class);
		doReturn(queue) //
				.when(emailTemplateSenderFactory).queued();
		doReturn(queue) //
				.when(queue).withAccount(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withTemplate(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withAttachments(any(Iterable.class));
		doReturn(queue) //
				.when(queue).withReference(anyLong());
		doReturn(queue) //
				.when(queue).withTemplateResolver(any(TemplateResolver.class));
		final EmailTemplateSenderFactory.EmailTemplateSender sender = mock(
				EmailTemplateSenderFactory.EmailTemplateSender.class);
		doReturn(sender) //
				.when(queue).build();

		// when
		underTest.create(task, true).execute();

		// then
		final ArgumentCaptor<Supplier> suppliers = ArgumentCaptor.forClass(Supplier.class);
		verify(emailTemplateSenderFactory).queued();
		verify(queue).withAccount(suppliers.capture());
		verify(queue).withTemplate(suppliers.capture());
		verify(queue).withAttachments(any(Iterable.class));
		verify(queue).withReference(eq(42L));
		verify(queue).withTemplateResolver(any(TemplateResolver.class));
		verify(queue).build();
		verify(sender).execute();

		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, reportLogic, dataView, databaseEngine,
				emailTemplateSenderFactory, queue, sender);
	}

	@Test
	public void templateIsReadedOnlyOnceAndTemplateAccountHasPriorityOverSpecifiedOne() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.withEmailTemplate("email template") //
				.withEmailAccount("email account") //
				.build();
		final EmailTemplateSenderFactory.Builder queue = mock(EmailTemplateSenderFactory.Builder.class);
		doReturn(queue) //
				.when(emailTemplateSenderFactory).queued();
		doReturn(queue) //
				.when(queue).withAccount(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withTemplate(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withAttachments(any(Iterable.class));
		doReturn(queue) //
				.when(queue).withReference(anyLong());
		doReturn(queue) //
				.when(queue).withTemplateResolver(any(TemplateResolver.class));
		final EmailTemplateSenderFactory.EmailTemplateSender sender = mock(
				EmailTemplateSenderFactory.EmailTemplateSender.class);
		doReturn(sender) //
				.when(queue).build();
		final EmailAccount account = mock(EmailAccount.class);
		doReturn(of(account)) // "
				.when(emailAccountFacade).firstOfOrDefault(any(Iterable.class));
		final EmailTemplateLogic.Template template = mock(EmailTemplateLogic.Template.class);
		doReturn("email account from template") //
				.when(template).getAccount();
		doReturn(template) //
				.when(emailTemplateLogic).read(anyString());

		// when
		underTest.create(task, true).execute();

		// then
		final ArgumentCaptor<Supplier> suppliers = ArgumentCaptor.forClass(Supplier.class);
		verify(emailTemplateSenderFactory).queued();
		verify(queue).withAccount(suppliers.capture());
		verify(queue).withTemplate(suppliers.capture());
		verify(queue).withAttachments(any(Iterable.class));
		verify(queue).withReference(eq(42L));
		verify(queue).withTemplateResolver(any(TemplateResolver.class));
		verify(queue).build();
		verify(sender).execute();

		assertThat((EmailAccount) suppliers.getAllValues().get(0).get(), equalTo(account));
		final ArgumentCaptor<Iterable> accounts = ArgumentCaptor.forClass(Iterable.class);
		verify(emailAccountFacade).firstOfOrDefault(accounts.capture());
		assertThat(elementsEqual(accounts.getValue(), asList("email account from template", "email account")),
				equalTo(true));

		assertThat((EmailTemplateLogic.Template) suppliers.getAllValues().get(1).get(), equalTo(template));
		verify(emailTemplateLogic).read(eq("email template"));

		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, reportLogic, dataView, databaseEngine,
				emailTemplateSenderFactory, queue, sender);
	}

	@Test
	public void reportLogicNotInvokedWhenReportIsNotActive() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.build();
		final EmailTemplateSenderFactory.Builder queue = mock(EmailTemplateSenderFactory.Builder.class);
		doReturn(queue) //
				.when(emailTemplateSenderFactory).queued();
		doReturn(queue) //
				.when(queue).withAccount(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withTemplate(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withAttachments(any(Iterable.class));
		doReturn(queue) //
				.when(queue).withReference(anyLong());
		doReturn(queue) //
				.when(queue).withTemplateResolver(any(TemplateResolver.class));
		final EmailTemplateSenderFactory.EmailTemplateSender sender = mock(
				EmailTemplateSenderFactory.EmailTemplateSender.class);
		doReturn(sender) //
				.when(queue).build();

		// when
		underTest.create(task, true).execute();

		// then
		final ArgumentCaptor<Iterable> iterables = ArgumentCaptor.forClass(Iterable.class);
		verify(emailTemplateSenderFactory).queued();
		verify(queue).withAccount(any(Supplier.class));
		verify(queue).withTemplate(any(Supplier.class));
		verify(queue).withAttachments(iterables.capture());
		verify(queue).withReference(eq(42L));
		verify(queue).withTemplateResolver(any(TemplateResolver.class));
		verify(queue).build();
		verify(sender).execute();

		final Iterable<?> attachments = iterables.getValue();
		assertThat(attachments.iterator().hasNext(), equalTo(false));

		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, reportLogic, dataView, databaseEngine,
				emailTemplateSenderFactory, queue, sender);
	}

	@Test
	public void reportLogicInvokedWhenReportIsActive() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance() //
				.withId(42L) //
				.withEmailActive(true) //
				.withReportActive(true) //
				.withReportName("report name") //
				.withReportExtension("pdf") //
				.withReportParameters(ChainablePutMap.of(new HashMap<String, String>())//
						.chainablePut("foo", "oof"))
				.build();
		final EmailTemplateSenderFactory.Builder queue = mock(EmailTemplateSenderFactory.Builder.class);
		doReturn(queue) //
				.when(emailTemplateSenderFactory).queued();
		doReturn(queue) //
				.when(queue).withAccount(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withTemplate(any(Supplier.class));
		doReturn(queue) //
				.when(queue).withAttachments(any(Iterable.class));
		doReturn(queue) //
				.when(queue).withReference(anyLong());
		doReturn(queue) //
				.when(queue).withTemplateResolver(any(TemplateResolver.class));
		final EmailTemplateSenderFactory.EmailTemplateSender sender = mock(
				EmailTemplateSenderFactory.EmailTemplateSender.class);
		doReturn(sender) //
				.when(queue).build();
		final Report report = mock(Report.class);
		doReturn(42) //
				.when(report).getId();
		doReturn("report name") //
				.when(report).getTitle();
		final Report anotherReport = mock(Report.class);
		doReturn("another report name") //
				.when(anotherReport).getTitle();
		doReturn(asList(report, anotherReport)) //
				.when(reportLogic).readAll();
		final DataHandler dataHandler = new DataHandler(new FileDataSource(tmp.newFile()));
		doReturn(dataHandler) //
				.when(reportLogic).download(anyInt(), any(Extension.class), anyMapOf(String.class, Object.class));

		// when
		underTest.create(task, true).execute();

		// then
		final ArgumentCaptor<Iterable> iterables = ArgumentCaptor.forClass(Iterable.class);
		verify(emailTemplateSenderFactory).queued();
		verify(queue).withAccount(any(Supplier.class));
		verify(queue).withTemplate(any(Supplier.class));
		verify(queue).withAttachments(iterables.capture());
		verify(queue).withReference(eq(42L));
		verify(queue).withTemplateResolver(any(TemplateResolver.class));
		verify(queue).build();
		verify(sender).execute();

		final Iterable<?> attachments = iterables.getValue();
		final DataHandler output = ((Supplier<DataHandler>) attachments.iterator().next()).get();
		verify(reportLogic).readAll();
		verify(reportLogic).download(eq(42), eq(ReportLogic.Extensions.pdf()),
				eq(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "oof")));
		assertThat(output, equalTo(dataHandler));

		verifyNoMoreInteractions(emailAccountFacade, emailTemplateLogic, reportLogic, dataView, databaseEngine,
				emailTemplateSenderFactory, queue, sender);
	}

}
