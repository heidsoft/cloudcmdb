package org.cmdbuild.servlets.json.translationtable.objects.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;

import javax.activation.DataHandler;

import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Lists;

public class DefaultCsvImporter implements CsvImporter {

	private static final int COMMA_SEPARATOR = ',';
	private static final String LINE_SEPARATOR = "\n";
	private static final char QUOTE_CHARACTER = '"';
	private final int columnSeparator;
	private final String lineSeparator;
	private final char quoteCharacter;
	private final DataHandler dataHandler;

	public static Builder newInstance() {
		return new Builder();
	}

	public DefaultCsvImporter(final Builder builder) {
		this.columnSeparator = (builder.separator != null) ? (int) builder.separator.charAt(0) : COMMA_SEPARATOR;
		this.lineSeparator = (builder.lineSeparator != null) ? builder.lineSeparator : LINE_SEPARATOR;
		this.quoteCharacter = (builder.quoteCharacter != null) ? builder.quoteCharacter.charAt(0) : QUOTE_CHARACTER;
		this.dataHandler = builder.dataHandler;
	}

	@Override
	public Iterable<CsvTranslationRecord> read() throws IOException {

		// TODO use new CSV service for reading and writing
		final Collection<CsvTranslationRecord> records = Lists.newArrayList();
		final CsvPreference preferences = new CsvPreference(quoteCharacter, columnSeparator, lineSeparator);
		final Reader reader = new InputStreamReader(dataHandler.getInputStream());
		final ICsvMapReader csvReader = new CsvMapReader(reader, preferences);
		try {
			final String[] headers = csvReader.getCSVHeader(true);
			Map currentLine = csvReader.read(headers);
			while (currentLine != null) {
				final CsvTranslationRecord record = new CsvTranslationRecord(currentLine);
				records.add(new CsvTranslationRecord(currentLine));
				currentLine = csvReader.read(headers);
			}
		} finally {
			csvReader.close();
		}
		return records;
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DefaultCsvImporter> {

		private DataHandler dataHandler;
		private String lineSeparator;
		private String quoteCharacter;
		private String separator;

		@Override
		public DefaultCsvImporter build() {
			return new DefaultCsvImporter(this);
		}

		public Builder withDataHandler(final DataHandler dataHandler) {
			this.dataHandler = dataHandler;
			return this;
		}

		public Builder withLineSeparator(final String lineSeparator) {
			this.lineSeparator = lineSeparator;
			return this;
		}

		public Builder withQuoteCharacter(final String quoteChar) {
			this.quoteCharacter = quoteChar;
			return this;
		}

		public Builder withSeparator(final String separator) {
			this.separator = separator;
			return this;
		}

	}

}
