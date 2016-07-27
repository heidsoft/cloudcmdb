package org.cmdbuild.dao.view.user;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entry.ForwardingRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.ClassHistory;
import org.cmdbuild.dao.query.clause.DomainHistory;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.AbstractDataView;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;

import com.google.common.base.Function;

public class UserDataView extends AbstractDataView {

	private final CMDataView view;
	private final PrivilegeContext privilegeContext;
	private final RowAndColumnPrivilegeFetcher rowColumnPrivilegeFetcher;
	private final OperationUser operationUser;

	public UserDataView( //
			final CMDataView view, //
			final PrivilegeContext privilegeContext, //
			final RowAndColumnPrivilegeFetcher rowPrivilegeFetcher, //
			final OperationUser operationUser //
	) {
		this.view = view;
		this.privilegeContext = privilegeContext;
		this.rowColumnPrivilegeFetcher = rowPrivilegeFetcher;
		this.operationUser = operationUser;
	}

	@Override
	protected CMDataView viewForBuilder() {
		return view;
	}

	public PrivilegeContext getPrivilegeContext() {
		return privilegeContext;
	}

	@Override
	public CMClass findClass(final Long id) {
		return proxy(view.findClass(id));
	}

	@Override
	public CMClass findClass(final String name) {
		return proxy(view.findClass(name));
	}

	@Override
	public CMClass findClass(final CMIdentifier identifier) {
		return proxy(view.findClass(identifier));
	}

	/**
	 * Returns the active and not active classes for which the user has read
	 * access. It does not return reserved classes
	 */
	@Override
	public Iterable<CMClass> findClasses() {
		return proxyClasses(view.findClasses());
	}

	@Override
	public CMClass create(final CMClassDefinition definition) {
		return proxy(view.create(definition));
	}

	@Override
	public CMClass update(final CMClassDefinition definition) {
		return proxy(view.update(definition));
	}

	@Override
	public CMAttribute createAttribute(final CMAttributeDefinition definition) {
		return proxy(view.createAttribute(definition));
	}

	@Override
	public CMAttribute updateAttribute(final CMAttributeDefinition definition) {
		return proxy(view.updateAttribute(definition));
	}

	@Override
	public void delete(final CMAttribute attribute) {
		view.delete(attribute);
	}

	@Override
	public CMDomain findDomain(final Long id) {
		return proxy(view.findDomain(id));
	}

	@Override
	public CMDomain findDomain(final String name) {
		return proxy(view.findDomain(name));
	}

	@Override
	public UserDomain findDomain(final CMIdentifier identifier) {
		return UserDomain.newInstance(this, view.findDomain(identifier));
	}

	/**
	 * Returns the active and not active domains. It does not return reserved
	 * domains
	 * 
	 * @return all domains (active and non active)
	 */
	@Override
	public Iterable<CMDomain> findDomains() {
		return proxyDomains(view.findDomains());
	}

	@Override
	public CMDomain create(final CMDomainDefinition definition) {
		return proxy(view.create(definition));
	}

	@Override
	public CMDomain update(final CMDomainDefinition definition) {
		return proxy(view.update(definition));
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return view.findFunctionByName(name);
	}

	/**
	 * Returns all the defined functions for every user.
	 */
	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return view.findAllFunctions();
	}

	@Override
	public void delete(final CMEntryType entryType) {
		view.delete(entryType);
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		// TODO
		return view.createCardFor(type);
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return view.update(card);
	}

	@Override
	public CMQueryResult executeQuery(final QuerySpecs querySpecs) {
		return UserQueryResult.newInstance(this, view.executeQuery(querySpecs));
	}

	@Override
	public Iterable<? extends WhereClause> getAdditionalFiltersFor(final CMEntryType classToFilter) {
		return rowColumnPrivilegeFetcher.fetchPrivilegeFiltersFor(classToFilter);
	}

	/*
	 * Proxy helpers
	 */

	/**
	 * Note that a UserClass is null if the user does not have the privileges to
	 * read the class or if the class is a system class (reserved)
	 * 
	 * @param source
	 * @return
	 */
	Iterable<CMClass> proxyClasses(final Iterable<? extends CMClass> source) {
		return from(source) //
				.transform(new Function<CMClass, CMClass>() {

					@Override
					public CMClass apply(final CMClass input) {
						return proxy(input);
					}

				}) //
				.filter(CMClass.class);
	}

	Iterable<CMDomain> proxyDomains(final Iterable<? extends CMDomain> source) {
		return from(source) //
				.transform(new Function<CMDomain, CMDomain>() {

					@Override
					public CMDomain apply(final CMDomain input) {
						return proxy(input);
					}

				}) //
				.filter(CMDomain.class);
	}

	Iterable<CMAttribute> proxyAttributes(final Iterable<? extends CMAttribute> source) {
		return from(source) //
				.transform(new Function<CMAttribute, CMAttribute>() {

					@Override
					public CMAttribute apply(final CMAttribute input) {
						return proxy(input);
					}

				}) //
				.filter(CMAttribute.class);
	}

	CMEntryType proxy(final CMEntryType entryType) {
		return new CMEntryTypeVisitor() {

			private CMEntryType proxy;

			public CMEntryType proxy() {
				entryType.accept(this);
				return proxy;
			}

			@Override
			public void visit(final CMClass type) {
				proxy = UserDataView.this.proxy(type);
			}

			@Override
			public void visit(final CMDomain type) {
				proxy = UserDataView.this.proxy(type);
			}

			@Override
			public void visit(final CMFunctionCall type) {
				proxy = UserDataView.this.proxy(type);
			}

		}.proxy();
	}

	CMClass proxy(final CMClass type) {
		final boolean historical = type instanceof ClassHistory;
		final CMClass nonHistoricType = historical ? ClassHistory.class.cast(type).getType() : type;
		final UserClass userType = UserClass.newInstance(this, nonHistoricType);
		return historical ? ClassHistory.of(userType) : userType;
	}

	CMDomain proxy(final CMDomain type) {
		final boolean historical = type instanceof DomainHistory;
		final CMDomain nonHistoricType = historical ? DomainHistory.class.cast(type).getType() : type;
		final UserDomain userType = UserDomain.newInstance(this, nonHistoricType);
		return historical ? DomainHistory.of(userType) : userType;
	}

	CMFunctionCall proxy(final CMFunctionCall type) {
		return UserFunctionCall.newInstance(this, type);
	}

	CMAttribute proxy(final CMAttribute attribute) {
		return UserAttribute.newInstance(this, attribute, rowColumnPrivilegeFetcher);
	}

	QuerySpecsBuilder proxy(final QuerySpecsBuilder querySpecsBuilder) {
		return UserQuerySpecsBuilder.newInstance(querySpecsBuilder, this);
	}

	QuerySpecs proxy(final QuerySpecs querySpecs) {
		return UserQuerySpecs.newInstance(this, querySpecs, operationUser, rowColumnPrivilegeFetcher);
	}

	FromClause proxy(final FromClause fromClause) {
		return UserFromClause.newInstance(this, fromClause);
	}

	@Override
	public CMRelationDefinition createRelationFor(final CMDomain domain) {
		// TODO check privileges
		return view.createRelationFor(domain);
	}

	@Override
	public CMRelationDefinition update(final CMRelation relation) {
		// TODO check privileges
		return view.update(relation);
	}

	@Override
	public void delete(final CMRelation relation) {
		// TODO: check privileges
		view.delete(proxyUser(relation));
	}

	private CMRelation proxyUser(final CMRelation delegate) {
		return new ForwardingRelation() {

			@Override
			protected CMRelation delegate() {
				return delegate;
			}

			@Override
			public String getUser() {
				return operationUser.getAuthenticatedUser().getUsername();
			}

		};
	}

	@Override
	public void clear(final CMEntryType type) {
		view.clear(type);
	}

	@Override
	public void delete(final CMCard card) {
		// TODO: check privileges
		view.delete(proxyUser(card));
	}

	private CMCard proxyUser(final CMCard delegate) {
		return new ForwardingCard() {

			@Override
			protected CMCard delegate() {
				return delegate;
			}

			@Override
			public String getUser() {
				return operationUser.getAuthenticatedUser().getUsername();
			}

		};
	}

	// TODO reconsider this solution

	@Override
	public CMClass getActivityClass() {
		return UserClass.newInstance(this, view.getActivityClass());
	}

	@Override
	public CMClass getReportClass() {
		return UserClass.newInstance(this, view.getReportClass());
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		return proxy(super.select(attrDef));
	}

}
