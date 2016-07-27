package org.cmdbuild.servlets.json.translationtable.objects.csv;

import java.io.IOException;
import java.util.Map;

public interface CsvImporter {

	Iterable<CsvTranslationRecord> read() throws IOException;

}
