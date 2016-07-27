package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ActiveQueryRelations;
import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.CreateReport;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingProcessInstance;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FunctionCall;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewProcessInstance;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.QueryAllLookup;
import org.cmdbuild.api.fluent.QueryClass;
import org.cmdbuild.api.fluent.ws.EntryTypeAttribute;
import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.SelectFolder;
import org.cmdbuild.common.api.mail.SelectMail;
import org.cmdbuild.common.api.mail.SendableNewMail;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingWorkflowApi extends ForwardingObject implements WorkflowApi {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingWorkflowApi() {
	}

	@Override
	protected abstract WorkflowApi delegate();

	@Override
	public NewCard newCard(final String className) {
		return delegate().newCard(className);
	}

	@Override
	public SendableNewMail newMail() {
		return delegate().newMail();
	}

	@Override
	public ExistingCard existingCard(final CardDescriptor descriptor) {
		return delegate().existingCard(descriptor);
	}

	@Override
	public ExistingCard existingCard(final String className, final int id) {
		return delegate().existingCard(className, id);
	}

	@Override
	public NewMailQueue newMailQueue() {
		return delegate().newMailQueue();
	}

	@Override
	public NewRelation newRelation(final String domainName) {
		return delegate().newRelation(domainName);
	}

	@Override
	public ExistingRelation existingRelation(final String domainName) {
		return delegate().existingRelation(domainName);
	}

	@Override
	public SelectFolder selectFolder(final String folder) {
		return delegate().selectFolder(folder);
	}

	@Override
	public QueryClass queryClass(final String className) {
		return delegate().queryClass(className);
	}

	@Override
	public Impersonate impersonate() {
		return delegate().impersonate();
	}

	@Override
	public Private soap() {
		return delegate().soap();
	}

	@Override
	public ReferenceType referenceTypeFrom(final Card card) {
		return delegate().referenceTypeFrom(card);
	}

	@Override
	public FunctionCall callFunction(final String functionName) {
		return delegate().callFunction(functionName);
	}

	@Override
	public ReferenceType referenceTypeFrom(final CardDescriptor cardDescriptor) {
		return delegate().referenceTypeFrom(cardDescriptor);
	}

	@Override
	public CreateReport createReport(final String title, final String format) {
		return delegate().createReport(title, format);
	}

	@Override
	public ReferenceType referenceTypeFrom(final Object idAsObject) {
		return delegate().referenceTypeFrom(idAsObject);
	}

	@Override
	public ActiveQueryRelations queryRelations(final CardDescriptor descriptor) {
		return delegate().queryRelations(descriptor);
	}

	@Override
	public CardDescriptor cardDescriptorFrom(final ReferenceType referenceType) {
		return delegate().cardDescriptorFrom(referenceType);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return delegate().selectMail(mail);
	}

	@Override
	public ActiveQueryRelations queryRelations(final String className, final int id) {
		return delegate().queryRelations(className, id);
	}

	@Override
	public Card cardFrom(final ReferenceType referenceType) {
		return delegate().cardFrom(referenceType);
	}

	@Override
	public NewProcessInstance newProcessInstance(final String processClassName) {
		return delegate().newProcessInstance(processClassName);
	}

	@Override
	public ExistingProcessInstance existingProcessInstance(final String processClassName, final int processId) {
		return delegate().existingProcessInstance(processClassName, processId);
	}

	@Override
	public QueryAllLookup queryLookup(final String type) {
		return delegate().queryLookup(type);
	}

	@Override
	public ClassInfo findClass(final String className) {
		return delegate().findClass(className);
	}

	@Override
	public ClassInfo findClass(final int classId) {
		return delegate().findClass(classId);
	}

	@Override
	public AttributeInfo findAttributeFor(final EntryTypeAttribute entryTypeAttribute) {
		return delegate().findAttributeFor(entryTypeAttribute);
	}

	@Override
	public LookupType selectLookupById(final int id) {
		return delegate().selectLookupById(id);
	}

	@Override
	public LookupType selectLookupByCode(final String type, final String code) {
		return delegate().selectLookupByCode(type, code);
	}

	@Override
	public LookupType selectLookupByDescription(final String type, final String description) {
		return delegate().selectLookupByDescription(type, description);
	}

}
