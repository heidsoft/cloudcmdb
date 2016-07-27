package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public interface ElementSerializer {

	Collection<? extends CsvTranslationRecord> serialize();

}
