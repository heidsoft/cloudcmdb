package org.cmdbuild.servlets.json.serializers.translations.table;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.json.JSONArray;

public class TranslationSerializerFactory {

	private static final String CLASS = "class";
	private static final String DOMAIN = "domain";
	private static final String FILTER = "filter";
	private static final String LOOKUP = "lookup";
	private static final String PROCESS = "process";
	private static final String VIEW = "view";
	private static final String REPORT = "report";
	private static final String MENU = "menu";

	private final boolean activeOnly;
	private final DataAccessLogic dataLogic;
	private final FilterLogic filterLogic;
	private final Iterable<String> languages;
	private final LookupStore lookupStore;
	private final TranslationLogic translationLogic;
	private final JSONArray sorters;
	private final String type;
	private final ViewLogic viewLogic;
	private final ReportStore reportStore;
	private final AuthenticationLogic authLogic;
	private final MenuLogic menuLogic;
	private final String separator;
	private final SetupFacade setupFacade;
	private final Output output;

	public static SerializerBuilder newInstance() {
		return new SerializerBuilder();
	}

	public static enum Sections {
		CLASS, PROCESS, DOMAIN, VIEW, FILTER, LOOKUP, REPORT, MENU;
	}

	public static enum Output {
		CSV, TABLE;
	}

	public TranslationSerializerFactory(final SerializerBuilder builder) {
		this.activeOnly = builder.activeOnly;
		this.authLogic = builder.authLogic;
		this.dataLogic = builder.dataLogic;
		this.filterLogic = builder.filterLogic;
		this.languages = builder.languages;
		this.lookupStore = builder.lookupStore;
		this.menuLogic = builder.menuLogic;
		this.output = builder.output;
		this.reportStore = builder.reportStore;
		this.separator = builder.separator;
		this.setupFacade = builder.setupFacade;
		this.sorters = builder.sorters;
		this.translationLogic = builder.translationLogic;
		this.type = builder.type;
		this.viewLogic = builder.viewLogic;
	}

	// TODO use the enum Section instead of the constants
	public TranslationSectionSerializer createSerializer() {
		if (type.equalsIgnoreCase(CLASS)) {
			if (output.equals(Output.TABLE)) {
				return new ClassTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.ClassSectionSerializer(dataLogic,
						activeOnly, translationLogic, sorters, separator, languages);
			}
		} else if (type.equalsIgnoreCase(DOMAIN)) {
			if (output.equals(Output.TABLE)) {
				return new DomainTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters, separator,
						setupFacade);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.DomainSectionSerializer(dataLogic,
						activeOnly, translationLogic, sorters, separator, languages);
			}
		} else if (type.equalsIgnoreCase(FILTER)) {
			if (output.equals(Output.TABLE)) {
				return new FilterTranslationSerializer(filterLogic, translationLogic, sorters, separator, setupFacade);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.FilterSectionSerializer(
						translationLogic, sorters, filterLogic, languages);
			}
		} else if (type.equalsIgnoreCase(LOOKUP)) {
			if (output.equals(Output.TABLE)) {
				return new LookupTranslationSerializer(lookupStore, activeOnly, translationLogic, sorters);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.LookupSectionSerializer(lookupStore,
						activeOnly, translationLogic, sorters, languages);
			}
		} else if (type.equalsIgnoreCase(MENU)) {
			if (output.equals(Output.TABLE)) {
				return new MenuTranslationSerializer(authLogic, menuLogic, translationLogic, sorters, separator,
						setupFacade);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.MenuSectionSerializer(authLogic,
						menuLogic, translationLogic, sorters, languages);
			}
		} else if (type.equalsIgnoreCase(PROCESS)) {
			if (output.equals(Output.TABLE)) {
				return new ProcessTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters, separator,
						setupFacade);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.ProcessSectionSerializer(dataLogic,
						activeOnly, translationLogic, sorters, separator, languages);
			}
		} else if (type.equalsIgnoreCase(REPORT)) {
			if (output.equals(Output.TABLE)) {
				return new ReportTranslationSerializer(reportStore, translationLogic, sorters, separator, setupFacade);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.ReportSectionSerializer(
						translationLogic, sorters, reportStore, languages);
			}
		} else if (type.equalsIgnoreCase(VIEW)) {
			if (output.equals(Output.TABLE)) {
				return new ViewTranslationSerializer(viewLogic, translationLogic, sorters, separator, setupFacade);
			} else if (output.equals(Output.CSV)) {
				return new org.cmdbuild.servlets.json.serializers.translations.csv.ViewSectionSerializer(
						translationLogic, sorters, viewLogic, languages);
			}
		}
		throw new IllegalArgumentException("type '" + type + "' unsupported");
	}

	public static final class SerializerBuilder implements Builder<TranslationSerializerFactory> {

		private boolean activeOnly;
		private AuthenticationLogic authLogic;
		private DataAccessLogic dataLogic;
		private FilterLogic filterLogic;
		private Iterable<String> languages;
		private LookupStore lookupStore;
		private MenuLogic menuLogic;
		public Output output;
		public String separator;
		private JSONArray sorters;
		private ReportStore reportStore;
		private TranslationLogic translationLogic;
		private String type;
		private ViewLogic viewLogic;
		private SetupFacade setupFacade;

		public SerializerBuilder withActiveOnly(final boolean activeOnly) {
			this.activeOnly = activeOnly;
			return this;
		}

		public SerializerBuilder withAuthLogic(final AuthenticationLogic authLogic) {
			this.authLogic = authLogic;
			return this;
		}

		public SerializerBuilder withDataAccessLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public SerializerBuilder withFilterLogic(final FilterLogic filterLogic) {
			this.filterLogic = filterLogic;
			return this;
		}

		public SerializerBuilder withSelectedLanguages(final Iterable<String> languages) {
			this.languages = languages;
			return this;
		}

		public SerializerBuilder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

		public SerializerBuilder withMenuLogic(final MenuLogic menuLogic) {
			this.menuLogic = menuLogic;
			return this;
		}

		public SerializerBuilder withOutput(final Output output) {
			this.output = output;
			return this;
		}

		public SerializerBuilder withReportStore(final ReportStore reportStore) {
			this.reportStore = reportStore;
			return this;
		}

		public SerializerBuilder withSeparator(final String separator) {
			this.separator = separator;
			return this;
		}

		public SerializerBuilder withSetupFacade(final SetupFacade setupFacade) {
			this.setupFacade = setupFacade;
			return this;
		}

		public SerializerBuilder withSorters(final JSONArray sorters) {
			this.sorters = sorters;
			return this;
		}

		public SerializerBuilder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public SerializerBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public SerializerBuilder withViewLogic(final ViewLogic viewLogic) {
			this.viewLogic = viewLogic;
			return this;
		}

		@Override
		public TranslationSerializerFactory build() {
			return new TranslationSerializerFactory(this);
		}

	}

}
