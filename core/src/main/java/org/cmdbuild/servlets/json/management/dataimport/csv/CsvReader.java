package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.io.IOException;
import java.util.Map.Entry;

import javax.activation.DataHandler;

public interface CsvReader {

	interface CsvLine {

		Iterable<Entry<String, String>> entries();

	}

	interface CsvData {

		Iterable<String> headers();

		Iterable<CsvLine> lines();

	}

	CsvData read(DataHandler dataHandler) throws IOException;

}
