package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupTypeQuery;
import org.cmdbuild.service.rest.v1.LookupTypes;
import org.cmdbuild.service.rest.v1.cxf.serialization.ToLookupTypeDetail;
import org.cmdbuild.service.rest.v1.model.LookupTypeDetail;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

public class CxfLookupTypes implements LookupTypes {

	private static final ToLookupTypeDetail TO_LOOKUP_TYPE_DETAIL = ToLookupTypeDetail.newInstance().build();

	private final ErrorHandler errorHandler;
	private final LookupLogic lookupLogic;

	public CxfLookupTypes(final ErrorHandler errorHandler, final LookupLogic lookupLogic) {
		this.errorHandler = errorHandler;
		this.lookupLogic = lookupLogic;
	}

	@Override
	public ResponseSingle<LookupTypeDetail> read(final String lookupTypeId) {
		final LookupType element = lookupLogic.typeFor(lookupTypeId);
		if (element == null) {
			errorHandler.lookupTypeNotFound(lookupTypeId);
		}
		return newResponseSingle(LookupTypeDetail.class) //
				.withElement(TO_LOOKUP_TYPE_DETAIL.apply(element)) //
				.build();
	}

	@Override
	public ResponseMultiple<LookupTypeDetail> readAll(final Integer limit, final Integer offset) {
		final PagedElements<LookupType> lookupTypes = lookupLogic.getAllTypes(new LookupTypeQuery() {

			@Override
			public Integer limit() {
				return limit;
			}

			@Override
			public Integer offset() {
				return offset;
			}

		});

		final Iterable<LookupTypeDetail> elements = from(lookupTypes) //
				.transform(TO_LOOKUP_TYPE_DETAIL);
		return newResponseMultiple(LookupTypeDetail.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(lookupTypes.totalSize())) //
						.build()) //
				.build();
	}

}
