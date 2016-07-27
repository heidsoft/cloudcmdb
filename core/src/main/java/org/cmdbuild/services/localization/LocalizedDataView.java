package org.cmdbuild.services.localization;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingClass;
import org.cmdbuild.dao.entrytype.ForwardingDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.translation.TranslationFacade;

import com.google.common.base.Function;

public class LocalizedDataView extends ForwardingDataView {

	private static class ToLocalizedAttribute implements Function<CMAttribute, CMAttribute> {

		private final TranslationFacade facade;
		private final Map<String, String> cacheForGroupsOptimization;

		public ToLocalizedAttribute(final TranslationFacade facade, final Map<String, String> cacheForGroupsOptimization) {
			this.facade = facade;
			this.cacheForGroupsOptimization = cacheForGroupsOptimization;
		}

		@Override
		public CMAttribute apply(final CMAttribute input) {
			return (input == null) ? null : new LocalizedAttribute(input, facade, cacheForGroupsOptimization);
		}

	}

	private final CMDataView delegate;
	private final TranslationFacade facade;
	private final Function<CMClass, CMClass> TO_LOCALIZED_CLASS;
	private final Function<CMDomain, CMDomain> TO_LOCALIZED_DOMAIN;
	private Function<CMQueryResult, CMQueryResult> TO_LOCALIZED_QUERYRESULT;

	public LocalizedDataView(final CMDataView delegate, final TranslationFacade facade, final LookupStore lookupStore) {
		this.delegate = delegate;
		this.facade = facade;
		this.TO_LOCALIZED_CLASS = new Function<CMClass, CMClass>() {

			@Override
			public CMClass apply(final CMClass input) {
				return (input == null) ? null : new LocalizedClass(proxyAttributes(input), facade);
			}

			private CMClass proxyAttributes(final CMClass input) {
				return new ForwardingClass() {

					@Override
					protected CMClass delegate() {
						return input;
					}

					@Override
					public Iterable<CMAttribute> getActiveAttributes() {
						return proxyAttribute(super.getActiveAttributes());
					}

					@Override
					public Iterable<CMAttribute> getAttributes() {
						return proxyAttribute(super.getAttributes());
					}

					@Override
					public Iterable<? extends CMAttribute> getAllAttributes() {
						return proxyAttribute(super.getAllAttributes());
					}

					@Override
					public CMAttribute getAttribute(final String name) {
						return proxyAttribute(super.getAttribute(name));
					}

				};
			}

		};
		this.TO_LOCALIZED_DOMAIN = new Function<CMDomain, CMDomain>() {

			@Override
			public CMDomain apply(final CMDomain input) {
				return (input == null) ? null : new LocalizedDomain(proxyAttributes(input), facade);
			}

			private CMDomain proxyAttributes(final CMDomain input) {
				return new ForwardingDomain() {

					@Override
					protected CMDomain delegate() {
						return input;
					}

					@Override
					public Iterable<CMAttribute> getActiveAttributes() {
						return proxyAttribute(super.getActiveAttributes());
					}

					@Override
					public Iterable<CMAttribute> getAttributes() {
						return proxyAttribute(super.getAttributes());
					}

					@Override
					public Iterable<? extends CMAttribute> getAllAttributes() {
						return proxyAttribute(super.getAllAttributes());
					}

					@Override
					public CMAttribute getAttribute(final String name) {
						return proxyAttribute(super.getAttribute(name));
					}

				};
			}

		};
		this.TO_LOCALIZED_QUERYRESULT = new Function<CMQueryResult, CMQueryResult>() {

			@Override
			public CMQueryResult apply(final CMQueryResult input) {
				return (input == null) ? null : new LocalizedQueryResult(input, facade, lookupStore);
			}

		};

	}

	@Override
	protected CMDataView delegate() {
		return delegate;
	}

	@Override
	public CMClass findClass(final Long id) {
		return proxyClass(super.findClass(id));
	}

	@Override
	public CMClass findClass(final String name) {
		return proxyClass(super.findClass(name));
	}

	@Override
	public CMClass findClass(final CMIdentifier identifier) {
		return proxyClass(super.findClass(identifier));
	}

	@Override
	public Iterable<? extends CMClass> findClasses() {
		return proxyClasses(super.findClasses());
	}

	@Override
	public CMClass create(final CMClassDefinition definition) {
		return proxyClass(super.create(definition));
	}

	@Override
	public CMClass update(final CMClassDefinition definition) {
		return proxyClass(super.update(definition));
	}

	@Override
	public CMClass getActivityClass() {
		return proxyClass(super.getActivityClass());
	}

	@Override
	public CMClass getReportClass() {
		return proxyClass(super.getReportClass());
	}

	@Override
	public CMDomain findDomain(final Long id) {
		return proxyDomain(super.findDomain(id));
	}

	@Override
	public CMDomain findDomain(final String name) {
		return proxyDomain(super.findDomain(name));
	}

	@Override
	public CMDomain findDomain(final CMIdentifier identifier) {
		return proxyDomain(super.findDomain(identifier));
	}

	@Override
	public Iterable<? extends CMDomain> findDomains() {
		return proxyDomains(super.findDomains());
	}

	@Override
	public CMQueryResult executeQuery(final QuerySpecs querySpecs) {
		return proxy(super.executeQuery(querySpecs));
	}

	private CMQueryResult proxy(final CMQueryResult executeQuery) {
		return TO_LOCALIZED_QUERYRESULT.apply(executeQuery);
	}

	private CMClass proxyClass(final CMClass type) {
		return TO_LOCALIZED_CLASS.apply(type);
	}

	private Iterable<CMClass> proxyClasses(final Iterable<? extends CMClass> types) {
		return from(types) //
				.transform(TO_LOCALIZED_CLASS) //
				.filter(CMClass.class);
	}

	private CMDomain proxyDomain(final CMDomain type) {
		return TO_LOCALIZED_DOMAIN.apply(type);
	}

	private Iterable<CMDomain> proxyDomains(final Iterable<? extends CMDomain> types) {
		return from(types) //
				.transform(TO_LOCALIZED_DOMAIN) //
				.filter(CMDomain.class);
	}

	private CMAttribute proxyAttribute(final CMAttribute attribute) {
		final Map<String, String> cacheForGroupsOptimization = newHashMap();
		return new ToLocalizedAttribute(facade, cacheForGroupsOptimization).apply(attribute);
	}

	private Iterable<CMAttribute> proxyAttribute(final Iterable<? extends CMAttribute> attributes) {
		final Map<String, String> cacheForGroupsOptimization = newHashMap();
		return from(attributes) //
				.transform(new ToLocalizedAttribute(facade, cacheForGroupsOptimization)) //
				.filter(CMAttribute.class);
	}

}
