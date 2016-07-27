package org.cmdbuild.servlets.json.translationtable.objects.csv;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

public class CsvTranslationRecord implements TranslationSerialization {

	private final Map<String, Object> map;

	public CsvTranslationRecord(final Map<String, Object> record) {
		this.map = record;
	}

	public Map<String, Object> getValues() {
		return map;
	}
	
	public String get(String key) {
		return String.class.cast(map.get(key));
	}
	
	public Set<Entry<String, Object>> getEntrySet(){
		return map.entrySet();
	}
	
	public Set<String> getKeySet(){
		return map.keySet();
	}
	
	@Override
	public String toString() {
		return map.toString();
	}

}
