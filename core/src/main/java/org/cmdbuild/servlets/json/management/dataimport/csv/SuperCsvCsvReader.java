package org.cmdbuild.servlets.json.management.dataimport.csv;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.UnmodifiableIterator;

public class SuperCsvCsvReader implements CsvReader {

	private static class Headers implements Iterable<String> {

		private final Iterable<String> headers;

		public Headers(final DataHandler dataHandler, final CsvPreference preferences) throws IOException {
			final Reader reader = new InputStreamReader(dataHandler.getInputStream());
			final ICsvMapReader csvReader = new CsvMapReader(reader, preferences);
			headers = newArrayList(csvReader.getCSVHeader(true));
			csvReader.close();
			reader.close();
		}

		@Override
		public Iterator<String> iterator() {
			return headers.iterator();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static class LinesIterator extends UnmodifiableIterator<CsvLine> {

		private final Reader reader;
		private final ICsvMapReader csvReader;
		private final String[] headers;

		private boolean closed;
		private Map<String, String> next;

		public LinesIterator(final DataHandler dataHandler, final CsvPreference preferences) throws IOException {
			reader = new InputStreamReader(dataHandler.getInputStream());
			csvReader = new CsvMapReader(reader, preferences);
			headers = csvReader.getCSVHeader(true);
			closed = false;
		}

		@Override
		public boolean hasNext() {
			try {
				final boolean hasNext;
				if (closed) {
					hasNext = true;
				} else {
					next = csvReader.read(headers);
					if (next == null) {
						csvReader.close();
						reader.close();
						closed = true;
					}
					hasNext = (next != null);
				}
				return hasNext;
			} catch (final IOException e) {
				// TODO log
				return false;
			}
		}

		@Override
		public CsvLine next() {
			return new CsvLineImpl(next);
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static class CsvLineImpl implements CsvLine {

		private final Map<String, String> map;

		public CsvLineImpl(final Map<String, String> map) {
			this.map = map;
		}

		@Override
		public Iterable<Entry<String, String>> entries() {
			return map.entrySet();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static class CsvDataImpl implements CsvData {

		private final Iterable<String> headers;
		private final Iterable<CsvLine> lines;

		public CsvDataImpl(final Iterable<String> headers, final Iterable<CsvLine> lines) {
			this.headers = headers;
			this.lines = lines;
		}

		@Override
		public Iterable<String> headers() {
			return headers;
		}

		@Override
		public Iterable<CsvLine> lines() {
			return lines;
		}

	}

	private final CsvPreference preferences;

	public SuperCsvCsvReader(final CsvPreference preferences) {
		this.preferences = preferences;
	}

	@Override
	public CsvData read(final DataHandler dataHandler) throws IOException {
		final Iterable<String> headers = new Headers(dataHandler, preferences);
		final Iterator<CsvLine> linesIterator = new LinesIterator(dataHandler, preferences);
		return new CsvDataImpl(headers, new Iterable<CsvLine>() {

			@Override
			public Iterator<CsvLine> iterator() {
				return linesIterator;
			}

		});
	}
}
