package org.cmdbuild.workflow.api;

import static java.lang.String.format;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.ForwardingFluentApi;
import org.cmdbuild.api.fluent.ws.EntryTypeAttribute;
import org.cmdbuild.common.Constants;
import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.SelectFolder;
import org.cmdbuild.common.api.mail.SelectMail;
import org.cmdbuild.common.api.mail.SendableNewMail;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

public class WorkflowApiImpl extends ForwardingFluentApi implements WorkflowApi {

	public static interface Context {

		FluentApi fluentApi();

		Private proxy();

		SchemaApi schemaApi();

		MailApi mailApi();

		/**
		 * Needed to perform other operations after the instantiation of the
		 * object.
		 */
		void callback(WorkflowApiImpl object);

		Context impersonate(String username, String group);

	}

	/**
	 * @deprecated use an instance of {@link Context} instead.
	 */
	@Deprecated
	public static Context context(final FluentApi fluentApi, final Private proxy, final SchemaApi schemaApi,
			final MailApi mailApi) {
		return new Context() {

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
				throw new UnsupportedOperationException();
			}

		};
	}

	@SuppressWarnings("serial")
	private static class ClassNotFound extends RuntimeException {

		public ClassNotFound(final int id) {
			super(format("class '%d' not found", id));
		}

	}

	private final Context context;
	private final FluentApi delegate;
	private final Private proxy;
	private final SchemaApi schemaApi;
	private final MailApi mailApi;

	/**
	 * It's really ugly but fortunately all is hidden behind the
	 * {@link SharkWorkflowApiFactory}.
	 */
	public WorkflowApiImpl(final Context context) {
		this.context = context;
		this.delegate = context.fluentApi();
		this.proxy = context.proxy();
		this.schemaApi = context.schemaApi();
		this.mailApi = context.mailApi();
		context.callback(this);
	}

	@Override
	protected FluentApi delegate() {
		return delegate;
	}

	@Override
	public Impersonate impersonate() {
		return new Impersonate() {

			private String username;
			private String group;

			@Override
			public Impersonate username(final String username) {
				this.username = username;
				return this;
			}

			@Override
			public Impersonate group(final String group) {
				this.group = group;
				return this;
			}

			@Override
			public WorkflowApi impersonate() {
				return new WorkflowApiImpl(context.impersonate(username, group));
			}

		};
	}

	@Override
	public Private soap() {
		return proxy;
	}

	@Override
	public ClassInfo findClass(final String className) {
		return schemaApi.findClass(className);
	}

	@Override
	public ClassInfo findClass(final int classId) {
		return schemaApi.findClass(classId);
	}

	@Override
	public AttributeInfo findAttributeFor(final EntryTypeAttribute entryTypeAttribute) {
		return schemaApi.findAttributeFor(entryTypeAttribute);
	}

	@Override
	public LookupType selectLookupById(final int id) {
		return (id <= 0) ? new LookupType() : schemaApi.selectLookupById(id);
	}

	@Override
	public LookupType selectLookupByCode(final String type, final String code) {
		return schemaApi.selectLookupByCode(type, code);
	}

	@Override
	public LookupType selectLookupByDescription(final String type, final String description) {
		return schemaApi.selectLookupByDescription(type, description);
	}

	@Override
	public SendableNewMail newMail() {
		return mailApi.newMail();
	}

	@Override
	public NewMailQueue newMailQueue() {
		return mailApi.newMailQueue();
	}

	@Override
	public SelectFolder selectFolder(final String folder) {
		return mailApi.selectFolder(folder);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return mailApi.selectMail(mail);
	}

	@Override
	public ReferenceType referenceTypeFrom(final Card card) {
		return referenceTypeFrom(card, card.getDescription());
	}

	@Override
	public ReferenceType referenceTypeFrom(final CardDescriptor cardDescriptor) {
		return referenceTypeFrom(cardDescriptor, null);
	}

	@Override
	public ReferenceType referenceTypeFrom(final Object idAsObject) {
		final int id = objectToInt(idAsObject);
		return (id <= 0) ? new ReferenceType()
				: referenceTypeFrom( //
						existingCard(Constants.BASE_CLASS_NAME, id) //
								.limitAttributes(Constants.DESCRIPTION_ATTRIBUTE) //
								.fetch());
	}

	private int objectToInt(final Object id) {
		final int idAsInt;
		if (id instanceof String) {
			idAsInt = Integer.parseInt(String.class.cast(id));
		} else if (id instanceof Number) {
			idAsInt = Number.class.cast(id).intValue();
		} else {
			throw new IllegalArgumentException(format("invalid class '%s' for id", id.getClass()));
		}
		return idAsInt;
	}

	private ReferenceType referenceTypeFrom(final CardDescriptor cardDescriptor, final String description) {
		return new ReferenceType( //
				cardDescriptor.getId(), //
				findClass(cardDescriptor.getClassName()).getId(), //
				(description == null) ? descriptionFor(cardDescriptor) : description);
	}

	private String descriptionFor(final CardDescriptor cardDescriptor) {
		return existingCard(cardDescriptor) //
				.limitAttributes(Constants.DESCRIPTION_ATTRIBUTE) //
				.fetch() //
				.getDescription();
	}

	@Override
	public CardDescriptor cardDescriptorFrom(final ReferenceType referenceType) {
		final ClassInfo classInfo = findClass(referenceType.getIdClass());
		final ClassInfo _classInfo;
		if (classInfo == null) {
			final ReferenceType fallbackReferenceType = referenceTypeFrom(referenceType.getId());
			_classInfo = findClass(fallbackReferenceType.getIdClass());
			if (_classInfo == null) {
				throw new ClassNotFound(referenceType.getIdClass());
			}
		} else {
			_classInfo = classInfo;
		}
		return new CardDescriptor( //
				_classInfo.getName(), //
				referenceType.getId());
	}

	@Override
	public Card cardFrom(final ReferenceType referenceType) {
		return existingCard(cardDescriptorFrom(referenceType)).fetch();
	}

}
