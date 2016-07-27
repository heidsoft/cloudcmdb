package org.cmdbuild.logic.report;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.report.ReportFactory.ReportExtension.PDF;
import static org.cmdbuild.report.ReportFactory.ReportType.CUSTOM;

import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.sql.DataSource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactoryDB;
import org.cmdbuild.report.ReportParameter;
import org.cmdbuild.report.ReportParameterConverter;
import org.cmdbuild.services.store.report.ReportStore;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class DefaultReportLogic implements ReportLogic {

	private static class ReportImpl implements Report {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<ReportImpl> {

			private int id;
			private String title;
			private String type;
			private String description;

			@Override
			public ReportImpl build() {
				return new ReportImpl(this);
			}

			public Builder setId(final int id) {
				this.id = id;
				return this;
			}

			public Builder setTitle(final String title) {
				this.title = title;
				return this;
			}

			public Builder setType(final String type) {
				this.type = type;
				return this;
			}

			public Builder setDescription(final String description) {
				this.description = description;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final int id;
		private final String title;
		private final String type;
		private final String description;

		private ReportImpl(final Builder builder) {
			this.id = builder.id;
			this.title = builder.title;
			this.type = builder.type;
			this.description = builder.description;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Report)) {
				return false;
			}
			final Report other = Report.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getTitle(), other.getTitle()) //
					.append(this.getType(), other.getType()) //
					.append(this.getDescription(), other.getDescription()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(id) //
					.append(title) //
					.append(type) //
					.append(description) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static final Function<org.cmdbuild.services.store.report.Report, Report> STORE_TO_LOGIC = new Function<org.cmdbuild.services.store.report.Report, Report>() {

		@Override
		public Report apply(final org.cmdbuild.services.store.report.Report input) {
			return ReportImpl.newInstance() //
					.setId(input.getId()) //
					.setTitle(input.getCode()) //
					// TODO do it better
					.setType(input.getType().toString()) //
					.setDescription(input.getDescription()) //
					.build();
		}

	};

	private static enum ExtensionConverter {
		CSV(ReportExtension.CSV), //
		ODT(ReportExtension.ODT), //
		PDF(ReportExtension.PDF), //
		RTF(ReportExtension.RTF), //
		ZIP(ReportExtension.ZIP), //
		UNDEFINED(null), //
		;

		private final ReportExtension value;

		private ExtensionConverter(final ReportExtension value) {
			this.value = value;
		}

		public ReportExtension reportExtension() {
			return value;
		}

		public static ExtensionConverter of(final Extension status) {
			return new ExtensionVisitor() {

				private ExtensionConverter output;

				public ExtensionConverter convert() {
					if (status != null) {
						status.accept(this);
					} else {
						output = UNDEFINED;
					}
					return output;
				}

				@Override
				public void visit(final Csv extension) {
					output = CSV;
				}

				@Override
				public void visit(final Odt extension) {
					output = ODT;
				}

				@Override
				public void visit(final Pdf extension) {
					output = PDF;
				}

				@Override
				public void visit(final Rtf extension) {
					output = RTF;
				}

				@Override
				public void visit(final Zip extension) {
					output = ZIP;
				}

			}.convert();
		}

	}

	private static final ReportExtension NOT_IMPORTANT = PDF;

	private final ReportStore reportStore;
	private final DataSource dataSource;
	private final CmdbuildConfiguration configuration;
	private final Predicate<org.cmdbuild.services.store.report.Report> readAllPredicate;

	public DefaultReportLogic(final ReportStore reportStore, final DataSource dataSource,
			final CmdbuildConfiguration configuration,
			final Predicate<org.cmdbuild.services.store.report.Report> readAllPredicate) {
		this.reportStore = reportStore;
		this.dataSource = dataSource;
		this.configuration = configuration;
		this.readAllPredicate = readAllPredicate;
	}

	@Override
	public Iterable<Report> readAll() {
		return from(reportStore.findReportsByType(CUSTOM)) //
				.filter(readAllPredicate) //
				.transform(STORE_TO_LOGIC);
	}

	@Override
	public Optional<Report> read(final int reportId) {
		try {
			return from(asList(reportStore.findReportById(reportId))) //
					.transform(STORE_TO_LOGIC) //
					.first();
		} catch (final Throwable e) {
			return Optional.absent();
		}
	}

	@Override
	public Iterable<CMAttribute> parameters(final int id) {
		try {
			return from(reportFactory(id).getReportParameters()) //
					.transform(new Function<ReportParameter, CMAttribute>() {

						@Override
						public CMAttribute apply(final ReportParameter input) {
							return ReportParameterConverter.of(input).toCMAttribute();
						}

					});
		} catch (final Exception e) {
			logger.error(marker, "error getting report parameters", e);
			throw new RuntimeException("error getting report parameters", e);
		}
	}

	@Override
	public DataHandler download(final int reportId, final Extension extension,
			final Map<String, ? extends Object> parameters) {
		try {
			final ReportExtension reportExtension = ExtensionConverter.of(extension).reportExtension();
			final ReportFactoryDB reportFactory = reportFactory(reportId, reportExtension);

			// parameters management
			for (final ReportParameter reportParameter : reportFactory.getReportParameters()) {
				for (final Entry<String, ? extends Object> param : parameters.entrySet()) {
					if (param.getKey().equals(reportParameter.getFullName())) {
						// update parameter
						reportParameter.parseValue(param.getValue());
					}
				}
			}

			reportFactory.fillReport();

			// filename management
			final String filename = new StringBuilder() //
					// name
					.append(reportFactory.getReportCard().getCode().replaceAll(" ", "")) //
					// extension
					.append("." + reportFactory.getReportExtension().toString().toLowerCase()) //
					.toString();

			final javax.activation.DataSource dataSource = TempDataSource.newInstance() //
					.withName(filename) //
					.withContentType(reportFactory.getContentType()) //
					.build();
			final OutputStream outputStream = dataSource.getOutputStream();
			reportFactory.sendReportToStream(outputStream);
			return new DataHandler(dataSource);
		} catch (final Exception e) {
			logger.error(marker, "error downloading report", e);
			throw new RuntimeException("error downloading report", e);
		}
	}

	private ReportFactoryDB reportFactory(final int id) {
		return reportFactory(id, NOT_IMPORTANT);
	}

	private ReportFactoryDB reportFactory(final int id, final ReportExtension extension) {
		try {
			return new ReportFactoryDB(dataSource, configuration, reportStore, id, extension);
		} catch (final Exception e) {
			logger.error(marker, "error creating report factory", e);
			throw new RuntimeException("error creating report factory", e);
		}
	}

}
