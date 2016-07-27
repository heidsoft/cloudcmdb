package org.cmdbuild.servlets.json.management.dataimport.csv;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.ForwardingCardDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.servlets.json.management.dataimport.CardFiller;
import org.joda.time.DateTime;
import org.json.JSONException;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class CSVImporter {

	public static class CsvCard extends ForwardingCardDefinition implements CMCard {

		private final CMClass type;
		private String user;
		private final Map<String, Object> values;

		public CsvCard(final CMClass type, final CMCardDefinition delegate) {
			super(delegate);
			this.type = type;
			this.values = newHashMap();
		}

		@Override
		public Long getId() {
			return null;
		}

		@Override
		public String getUser() {
			return user;
		}

		@Override
		public CMCardDefinition setUser(final String user) {
			this.user = user;
			return super.setUser(user);
		}

		@Override
		public DateTime getBeginDate() {
			return null;
		}

		@Override
		public DateTime getEndDate() {
			return null;
		}

		@Override
		public Iterable<Entry<String, Object>> getAllValues() {
			return values.entrySet();
		}

		@Override
		public CMCardDefinition set(final Iterable<? extends Entry<String, ? extends Object>> keysAndValues) {
			for (final Entry<String, ? extends Object> element : keysAndValues) {
				values.put(element.getKey(), element.getValue());
			}
			return super.set(keysAndValues);
		}

		@Override
		public Object get(final String key) {
			return values.get(key);
		}

		@Override
		public CMCardDefinition set(final String key, final Object value) {
			values.put(key, value);
			return super.set(key, value);
		}

		@Override
		public <T> T get(final String key, final Class<? extends T> requiredType) {
			return requiredType.cast(get(key));
		}

		@Override
		public <T> T get(final String key, final Class<? extends T> requiredType, final T defaultValue) {
			return defaultIfNull(get(key, requiredType), defaultValue);
		}

		@Override
		public Iterable<Entry<String, Object>> getValues() {
			return from(getAllValues()) //
					.filter(new Predicate<Map.Entry<String, Object>>() {
						@Override
						public boolean apply(final Entry<String, Object> input) {
							final String name = input.getKey();
							final CMAttribute attribute = type.getAttribute(name);
							return !attribute.isSystem();
						}
					});
		}

		@Override
		public CMClass getType() {
			return type;
		}

		@Override
		public Object getCode() {
			return values.get(CODE_ATTRIBUTE);
		}

		@Override
		public CMCardDefinition setCode(final Object value) {
			values.put(CODE_ATTRIBUTE, value);
			return super.setCode(value);
		}

		@Override
		public Object getDescription() {
			return values.get(DESCRIPTION_ATTRIBUTE);
		}

		@Override
		public CMCardDefinition setDescription(final Object value) {
			values.put(DESCRIPTION_ATTRIBUTE, value);
			return super.setDescription(value);
		}

		@Override
		public Long getCurrentId() {
			return null;
		}

	}

	// a casual number from which start
	private static Long idCounter = 1000L;

	private final CsvReader csvReaded;
	private final CMDataView view;
	private final CMClass importClass;
	private final LookupStore lookupStore;

	public CSVImporter(final CsvReader csvReader, final CMDataView view, final LookupStore lookupStore,
			final CMClass importClass) {
		this.csvReaded = csvReader;
		this.view = view;
		this.lookupStore = lookupStore;
		this.importClass = importClass;
	}

	public CSVData getCsvDataFrom(final DataHandler csvFile) throws IOException, JSONException {
		final CsvReader.CsvData data = csvReaded.read(csvFile);
		return new CSVData(data.headers(), getCsvCardsFrom(data.lines()), importClass.getName());
	}

	private Map<Long, CSVCard> getCsvCardsFrom(final Iterable<CsvReader.CsvLine> lines) throws JSONException {
		final Map<Long, CSVCard> csvCards = Maps.newHashMap();
		for (final CsvReader.CsvLine line : lines) {
			final CardFiller cardFiller = new CardFiller(importClass, view, lookupStore);
			final Long fakeId = getAndIncrementIdForCsvCard();
			final CsvCard mutableCard = new CsvCard(importClass, view.createCardFor(importClass));
			final CSVCard csvCard = new CSVCard(mutableCard, fakeId);
			for (final Entry<String, String> entry : line.entries()) {
				try {
					cardFiller.fillCardAttributeWithValue( //
							mutableCard, //
							entry.getKey(), //
							entry.getValue() //
							);
				} catch (final CardFiller.CardFillerException ex) {
					csvCard.addInvalidAttribute(ex.attributeName, ex.attributeValue);
				}
			}
			csvCards.put(fakeId, csvCard);
		}

		return csvCards;
	}

	private static synchronized Long getAndIncrementIdForCsvCard() {
		return idCounter++;
	}

}