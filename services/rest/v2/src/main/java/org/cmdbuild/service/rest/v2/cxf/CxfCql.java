package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.transformEntries;
import static org.cmdbuild.service.rest.v2.cxf.util.Json.safeJsonArray;
import static org.cmdbuild.service.rest.v2.cxf.util.Json.safeJsonObject;
import static org.cmdbuild.service.rest.v2.model.Models.newCard;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.v2.Cql;
import org.cmdbuild.service.rest.v2.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.cmdbuild.service.rest.v2.model.Card;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

import com.google.common.base.Function;
import com.google.common.collect.Maps.EntryTransformer;

public class CxfCql implements Cql, LoggingSupport {

	private final DataAccessLogic dataAccessLogic;

	public CxfCql(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public ResponseMultiple<Card> read(final String filter, final String sort, final Integer limit, final Integer offset) {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(safeJsonObject(filter)) //
				.orderBy(safeJsonArray(sort)) //
				.limit(limit) //
				.offset(offset) //
				.build();
		final PagedElements<org.cmdbuild.model.data.Card> response = dataAccessLogic.fetchCards(null, queryOptions);
		final Iterable<Card> elements = from(response.elements()) //
				.transform(new Function<org.cmdbuild.model.data.Card, Card>() {

					@Override
					public Card apply(final org.cmdbuild.model.data.Card input) {
						return newCard() //
								.withType(input.getType().getName()) //
								.withId(input.getId()) //
								.withValues(adaptOutputValues(input.getType(), input)) //
								.build();
					}

				});
		return newResponseMultiple(Card.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(response.totalSize())) //
						.build()) //
				.build();
	}

	private Iterable<? extends Entry<String, Object>> adaptOutputValues(final CMClass targetClass,
			final org.cmdbuild.model.data.Card card) {
		return adaptOutputValues(targetClass, card.getAttributes());
	}

	private Iterable<? extends Entry<String, Object>> adaptOutputValues(final CMClass targetClass,
			final Map<String, Object> values) {
		return transformEntries(values, new EntryTransformer<String, Object, Object>() {

			@Override
			public Object transformEntry(final String key, final Object value) {
				final CMAttribute attribute = targetClass.getAttribute(key);
				final Object _value;
				if (attribute == null) {
					_value = value;
				} else {
					final CMAttributeType<?> attributeType = attribute.getType();
					_value = DefaultConverter.newInstance() //
							.build() //
							.toClient() //
							.convert(attributeType, value);
				}
				return _value;
			}

		}).entrySet();
	}

}
