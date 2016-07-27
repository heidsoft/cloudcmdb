package org.cmdbuild.servlets.json.serializers.translations.csv;

import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

import com.google.common.base.Optional;

public interface FieldSerializer {

	Optional<CsvTranslationRecord> serialize();

}
