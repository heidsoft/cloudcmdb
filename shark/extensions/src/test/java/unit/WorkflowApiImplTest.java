package unit;

import static org.cmdbuild.workflow.api.WorkflowApiImpl.context;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExecutorBasedFluentApi;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SchemaApi.ClassInfo;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.cmdbuild.workflow.api.WorkflowApiImpl;
import org.cmdbuild.workflow.api.WorkflowApiImpl.Context;
import org.cmdbuild.workflow.type.ReferenceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WorkflowApiImplTest {

	private static final String CLASS_NAME = "className";
	private static final int ID_CLASS = CLASS_NAME.hashCode();
	private static final int CARD_ID = 123;
	private static final String DESCRIPTION = "description";

	private FluentApiExecutor fluentApiExecutor;
	private Private proxy;
	private SchemaApi schemaApi;
	private MailApi mailApi;

	private FluentApi fluentApi;
	private WorkflowApi workflowApi;

	@Before
	public void createWorkflowApi() throws Exception {
		fluentApiExecutor = mock(FluentApiExecutor.class);
		proxy = mock(Private.class);
		schemaApi = mock(SchemaApi.class);
		mailApi = mock(MailApi.class);

		fluentApi = new ExecutorBasedFluentApi(fluentApiExecutor);
		workflowApi = new WorkflowApiImpl(context(fluentApi, proxy, schemaApi, mailApi));
	}

	@Test
	public void callbackInvokedOnCreation() throws Exception {
		// given
		final AtomicReference<WorkflowApiImpl> reference = new AtomicReference<>();
		final Context context = new Context() {

			@Override
			public FluentApi fluentApi() {
				return fluentApi;
			}

			@Override
			public Private proxy() {
				return proxy;
			}

			@Override
			public SchemaApi schemaApi() {
				return schemaApi;
			}

			@Override
			public MailApi mailApi() {
				return mailApi;
			}

			@Override
			public void callback(final WorkflowApiImpl object) {
				reference.set(object);
			}

			@Override
			public Context impersonate(final String username, final String group) {
				throw new UnsupportedOperationException();
			}

		};

		// then
		assertThat(reference.get(), equalTo(null));

		// when
		workflowApi = new WorkflowApiImpl(context);

		// then
		assertThat(reference.get(), equalTo(workflowApi));
	}

	@Test
	public void impersonateInvoked() throws Exception {
		// given
		final AtomicReference<String> _username = new AtomicReference<>();
		final AtomicReference<String> _group = new AtomicReference<>();
		final Context context = new Context() {

			@Override
			public FluentApi fluentApi() {
				return fluentApi;
			}

			@Override
			public Private proxy() {
				return proxy;
			}

			@Override
			public SchemaApi schemaApi() {
				return schemaApi;
			}

			@Override
			public MailApi mailApi() {
				return mailApi;
			}

			@Override
			public void callback(final WorkflowApiImpl object) {
				// nothing to do
			}

			@Override
			public Context impersonate(final String username, final String group) {
				_username.set(username);
				_group.set(group);
				// not important
				return this;
			}

		};

		// then
		assertThat(_username.get(), equalTo(null));
		assertThat(_group.get(), equalTo(null));

		// when
		workflowApi = new WorkflowApiImpl(context) //
				.impersonate() //
				.username("foo") //
				.group("bar") //
				.impersonate();

		// then
		assertThat(_username.get(), equalTo("foo"));
		assertThat(_group.get(), equalTo("bar"));
	}

	@Test
	public void findClassByNameAndExistingCardWhenConvertingFromCardDescriptorToReferenceType() throws Exception {
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));
		when(fluentApiExecutor.fetch(any(ExistingCard.class)))
				.thenReturn(cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));

		final CardDescriptor cardDescriptor = new CardDescriptor(CLASS_NAME, CARD_ID);
		final ReferenceType referenceType = workflowApi.referenceTypeFrom(cardDescriptor);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verify(fluentApiExecutor).fetch(any(ExistingCard.class));
		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByNameAndExistingCardWhenConvertingFromCardWithNoDescriptionToReferenceType()
			throws Exception {
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));
		when(fluentApiExecutor.fetch(any(ExistingCard.class)))
				.thenReturn(cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));

		final Card card = cardWithNoDescription(CLASS_NAME, CARD_ID);
		final ReferenceType referenceType = workflowApi.referenceTypeFrom(card);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verify(fluentApiExecutor).fetch(any(ExistingCard.class));
		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByNameWhenConvertingFromCardWithDescriptionToReferenceType() throws Exception {
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));

		final Card card = cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION);
		final ReferenceType referenceType = workflowApi.referenceTypeFrom(card);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void fetchCardFromBaseClassWhenConvertingFromIdToReferenceType() throws Exception {
		when(fluentApiExecutor.fetch(any(ExistingCard.class)))
				.thenReturn(cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));
		when(schemaApi.findClass(CLASS_NAME)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));

		final ReferenceType referenceType = workflowApi.referenceTypeFrom(CARD_ID);

		assertThat(referenceType.getId(), equalTo(CARD_ID));
		assertThat(referenceType.getIdClass(), equalTo(ID_CLASS));

		verify(fluentApiExecutor).fetch(any(ExistingCard.class));
		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(CLASS_NAME);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByIdWhenConvertingFromReferenceTypeToCardDescriptor() throws Exception {
		when(schemaApi.findClass(ID_CLASS)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));

		final ReferenceType referenceType = new ReferenceType(CARD_ID, ID_CLASS, DESCRIPTION);
		final CardDescriptor cardDescriptor = workflowApi.cardDescriptorFrom(referenceType);

		assertThat(cardDescriptor.getId(), equalTo(CARD_ID));
		assertThat(cardDescriptor.getClassName(), equalTo(CLASS_NAME));

		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(ID_CLASS);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	@Test
	public void findClassByIdAndExistingCardWhenConvertingFromReferenceTypeToCard() throws Exception {
		when(schemaApi.findClass(ID_CLASS)).thenReturn(newClassInfo(CLASS_NAME, ID_CLASS));
		when(fluentApiExecutor.fetch(any(ExistingCard.class)))
				.thenReturn(cardWithDescription(CLASS_NAME, CARD_ID, DESCRIPTION));

		final ReferenceType referenceType = new ReferenceType(CARD_ID, ID_CLASS, DESCRIPTION);
		final Card card = workflowApi.cardFrom(referenceType);

		assertThat(card.getId(), equalTo(CARD_ID));
		assertThat(card.getClassName(), equalTo(CLASS_NAME));
		assertThat(card.getDescription(), equalTo(DESCRIPTION));

		final ArgumentCaptor<ExistingCard> cardCaptor = ArgumentCaptor.forClass(ExistingCard.class);
		verify(fluentApiExecutor).fetch(cardCaptor.capture());

		final Card fetchedCard = cardCaptor.getValue();
		assertThat(fetchedCard.getClassName(), equalTo(CLASS_NAME));
		assertThat(fetchedCard.getId(), equalTo(CARD_ID));
		assertThat(fetchedCard.getAttributeNames().size(), equalTo(0));

		verifyNoMoreInteractions(fluentApiExecutor);
		verify(schemaApi).findClass(ID_CLASS);
		verifyNoMoreInteractions(schemaApi);
		verifyNoMoreInteractions(mailApi);
	}

	/*
	 * Utils
	 */

	private ClassInfo newClassInfo(final String className, final int idClass) {
		return new SchemaApi.ClassInfo(className, idClass);
	}

	private Card cardWithNoDescription(final String className, final int cardId) {
		return fluentApi.existingCard(className, cardId);
	}

	private Card cardWithDescription(final String className, final int cardId, final String description) {
		return fluentApi.existingCard(className, cardId).withDescription(description);
	}

}
