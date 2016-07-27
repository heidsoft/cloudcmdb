package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.service.rest.v2.model.Models.newLongIdAndDescription;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newReport;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.v2.model.Models.newValues;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.filter.json.JsonParser;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.data.access.filter.model.Filter;
import org.cmdbuild.logic.data.access.filter.model.Parser;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.report.ExtensionConverter;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.report.StringExtensionConverter;
import org.cmdbuild.service.rest.v2.Reports;
import org.cmdbuild.service.rest.v2.cxf.filter.ReportElementPredicate;
import org.cmdbuild.service.rest.v2.cxf.serialization.AttributeTypeResolver;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToAttributeDetail;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.JsonValues;
import org.cmdbuild.service.rest.v2.model.LongIdAndDescription;
import org.cmdbuild.service.rest.v2.model.Report;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Values;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfReports implements Reports {

	private static final AttributeTypeResolver ATTRIBUTE_TYPE_RESOLVER = new AttributeTypeResolver();

	private static final Values NO_PARAMETERS = newValues().build();

	private final ReportLogic logic;
	private final ErrorHandler errorHandler;
	private final ToAttributeDetail toAttributeDetail;

	public CxfReports(final ErrorHandler errorHandler, final ReportLogic logic, final CMDataView dataView,
			final LookupLogic lookupLogic) {
		this.logic = logic;
		this.errorHandler = errorHandler;
		this.toAttributeDetail = ToAttributeDetail.newInstance() //
				.withAttributeTypeResolver(ATTRIBUTE_TYPE_RESOLVER) //
				.withDataView(dataView) //
				.withErrorHandler(errorHandler) //
				.withLookupLogic(lookupLogic) //
				.build();
	}

	@Override
	public ResponseMultiple<LongIdAndDescription> readAll(final String filter, final Integer limit, final Integer offset) {
		final Predicate<ReportLogic.Report> predicate;
		if (isNotBlank(filter)) {
			final Parser parser = new JsonParser(filter);
			final Filter filterModel = parser.parse();
			final Optional<Element> element = filterModel.attribute();
			if (element.isPresent()) {
				predicate = new ReportElementPredicate(element.get());
			} else {
				predicate = alwaysTrue();
			}
		} else {
			predicate = alwaysTrue();
		}
		final Iterable<ReportLogic.Report> elements = from(logic.readAll()) //
				.filter(predicate);
		return newResponseMultiple(LongIdAndDescription.class) //
				.withElements(from(elements) //
						.transform(new Function<ReportLogic.Report, LongIdAndDescription>() {

							@Override
							public LongIdAndDescription apply(final ReportLogic.Report input) {
								return newLongIdAndDescription() //
										.withId(Long.valueOf(input.getId())) //
										.withDescription(input.getDescription()) //
										.build();
							}

						}) //
						.skip(defaultIfNull(offset, 0)) //
						.limit(defaultIfNull(limit, MAX_VALUE)) //
				) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attribute> readAllAttributes(final Long reportId, final Integer limit, final Integer offset) {
		final Optional<ReportLogic.Report> report = logic.read(reportId.intValue());
		if (!report.isPresent()) {
			errorHandler.reportNotFound(reportId);
		}
		final Iterable<CMAttribute> elements = logic.parameters(reportId.intValue());
		return newResponseMultiple(Attribute.class) //
				.withElements(from(elements) //
						.skip(defaultIfNull(offset, 0)) //
						.limit(defaultIfNull(limit, MAX_VALUE)) //
						.transform(toAttributeDetail)) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Report> read(final Long reportId) {
		final Optional<ReportLogic.Report> report = logic.read(reportId.intValue());
		if (!report.isPresent()) {
			errorHandler.reportNotFound(reportId);
		}
		final ReportLogic.Report _report = report.get();
		return newResponseSingle(Report.class) //
				.withElement(newReport() //
						.withId(Long.valueOf(_report.getId())) //
						.withTitle(_report.getTitle()) //
						.withDescription(_report.getDescription()) //
						.build()).build();
	}

	@Override
	public DataHandler download(final Long reportId, final String extension, final JsonValues parameters) {
		final Optional<ReportLogic.Report> report = logic.read(reportId.intValue());
		if (!report.isPresent()) {
			errorHandler.reportNotFound(reportId);
		}
		final ExtensionConverter extensionConverter = StringExtensionConverter.of(extension);
		if (!extensionConverter.isValid()) {
			errorHandler.extensionNotFound(extension);
		}
		return logic.download(reportId.intValue(), extensionConverter.extension(),
				defaultIfNull(parameters, NO_PARAMETERS));
	}
}
