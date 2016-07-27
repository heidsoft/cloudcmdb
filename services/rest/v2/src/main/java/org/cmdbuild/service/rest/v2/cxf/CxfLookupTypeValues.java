package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupQuery;
import org.cmdbuild.service.rest.v2.LookupTypeValues;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToLookupDetail;
import org.cmdbuild.service.rest.v2.model.LookupDetail;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

public class CxfLookupTypeValues implements LookupTypeValues {

	private static final ToLookupDetail TO_LOOKUP_DETAIL = ToLookupDetail.newInstance().build();

	private final ErrorHandler errorHandler;
	private final LookupLogic lookupLogic;

	public CxfLookupTypeValues(final ErrorHandler errorHandler, final LookupLogic lookupLogic) {
		this.errorHandler = errorHandler;
		this.lookupLogic = lookupLogic;
	}

	@Override
	public ResponseSingle<LookupDetail> read(final String lookupTypeId, final Long lookupValueId) {
		final Lookup lookup = lookupLogic.getLookup(lookupValueId);
		final LookupDetail element = TO_LOOKUP_DETAIL.apply(lookup);
		return newResponseSingle(LookupDetail.class) //
				.withElement(element) //
				.build();
	}

	@Override
	public ResponseMultiple<LookupDetail> readAll(final String lookupTypeId, final boolean activeOnly,
			final Integer limit, final Integer offset) {
		final LookupType found = lookupLogic.typeFor(lookupTypeId);
		if (found == null) {
			errorHandler.lookupTypeNotFound(lookupTypeId);
		}
		final LookupType lookupType = LookupType.newInstance().withName(lookupTypeId).build();
		final PagedElements<Lookup> lookups = lookupLogic.getAllLookup(lookupType, activeOnly, new LookupQuery() {

			@Override
			public Integer limit() {
				return limit;
			}

			@Override
			public Integer offset() {
				return offset;
			}

		});

		final Iterable<LookupDetail> elements = from(lookups) //
				.transform(TO_LOOKUP_DETAIL);
		return newResponseMultiple(LookupDetail.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(lookups.totalSize())) //
						.build()) //
				.build();
	}
}
