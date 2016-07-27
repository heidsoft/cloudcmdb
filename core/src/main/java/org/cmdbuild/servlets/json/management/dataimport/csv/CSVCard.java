package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.util.Map;

import org.cmdbuild.servlets.json.management.dataimport.csv.CSVImporter.CsvCard;

import com.google.common.collect.Maps;

public class CSVCard {

	private final Map<String, Object> invalidAttributes = Maps.newHashMap();
	private final CsvCard card;
	private final Long fakeId;

	public CSVCard(final CsvCard card, final Long fakeId) {
		this.card = card;
		this.fakeId = fakeId;
	}

	public CsvCard getCMCard() {
		return card;
	}

	public Long getFakeId() {
		return fakeId;
	}

	public Map<String, Object> getInvalidAttributes() {
		return invalidAttributes;
	}

	public void addInvalidAttribute(final String name, final Object value) {
		invalidAttributes.put(name, value);
	}

}
