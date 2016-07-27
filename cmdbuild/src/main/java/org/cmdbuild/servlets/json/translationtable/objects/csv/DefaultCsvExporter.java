package org.cmdbuild.servlets.json.translationtable.objects.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

public class DefaultCsvExporter implements CsvExporter {

	private static final String TEXT_CSV = "text/csv";
	private static final int COMMA_SEPARATOR = ',';
	private static final String LINE_SEPARATOR = "\n";
	private static final char QUOTE_CHARACTER = '"';
	private final Iterable<Map<String, Object>> records;
	private final int columnSeparator;
	private final String lineSeparator;
	private final char quoteCharacter;
	private final File file;
	private final String[] headers;
	private Iterable<String> languages;

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DefaultCsvExporter> {

		private Iterable<Map<String, Object>> records;
		private String separator;
		private String lineSeparator;
		private String quoteCharacter;
		private File file;
		private String[] headers;
		private Iterable<String> languages;

		@Override
		public DefaultCsvExporter build() {
			return new DefaultCsvExporter(this);
		}

		public Builder withRecords(final Iterable<Map<String, Object>> records) {
			this.records = records;
			return this;
		}

		public Builder withSeparator(final String separator) {
			this.separator = separator;
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

		public Builder withFile(final File file) {
			this.file = file;
			return this;
		}

		public Builder withHeaders(final String[] headers) {
			this.headers = headers;
			return this;
		}
		
		public Builder withLanguages(final Iterable<String> languages) {
			this.languages = languages;
			return this;
		}

	}

	public DefaultCsvExporter(final Builder builder) {
		this.columnSeparator = (builder.separator != null) ? (int) builder.separator.charAt(0) : COMMA_SEPARATOR;
		this.file = builder.file;
		this.headers = builder.headers;
		this.lineSeparator = (builder.lineSeparator != null) ? builder.lineSeparator : LINE_SEPARATOR;
		this.quoteCharacter = (builder.quoteCharacter != null) ? builder.quoteCharacter.charAt(0) : QUOTE_CHARACTER;
		this.records = builder.records;
		this.languages = builder.languages;

	}

	@Override
	public DataHandler write() throws IOException {
		final CsvPreference exportCsvPrefs = new CsvPreference(quoteCharacter, columnSeparator, lineSeparator);
		final ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file), exportCsvPrefs);
		writer.writeHeader(headers);
		for (final Map<String, Object> row : records) {
			writer.write(row, headers);
		}
		writer.close();
		final FileInputStream inputStream = new FileInputStream(file);
		final ByteArrayDataSource ds = new ByteArrayDataSource(inputStream, TEXT_CSV);
		ds.setName(file.getName());
		return new DataHandler(ds);
	}

}
