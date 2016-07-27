package org.cmdbuild.servlets.json.management;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.HEADERS;
import static org.cmdbuild.servlets.json.CommunicationConstants.SEPARATOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

public class ExportCSV extends JSONBaseWithSpringContext {

	private static final String TEXT_CSV = "text/csv";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@JSONExported(contentType = TEXT_CSV)
	public DataHandler export( //
			@Parameter(SEPARATOR) final String separator, //
			@Parameter(CLASS_NAME) final String className) //
			throws IOException {
		final File csvFile = systemDataAccessLogic().exportClassAsCsvFile(className, separator);
		return createDataHandler(csvFile);
	}

	private DataHandler createDataHandler(final File file) throws FileNotFoundException, IOException {
		final FileInputStream in = new FileInputStream(file);
		final ByteArrayDataSource ds = new ByteArrayDataSource(in, TEXT_CSV);
		ds.setName(file.getName());
		return new DataHandler(ds);
	}

	@JSONExported(contentType = TEXT_CSV)
	public DataHandler writeCsv( //
			@Parameter(FILE_NAME) final String fileName, //
			@Parameter(SEPARATOR) final String separator, //
			@Parameter(HEADERS) final String headersAsString, //
			@Parameter(DATA) final String data //
	) throws JsonParseException, JsonMappingException, IOException {
		final String[] headers = MAPPER.readValue(headersAsString, String[].class);
		final Map<String, Object>[] entries = MAPPER.readValue(data, Map[].class);
		final CsvPreference preferences = new CsvPreference('"', separator.charAt(0), "\n");
		final TempDataSource tmp = TempDataSource.newInstance() //
				.withName(fileName) //
				.build();
		final File file = tmp.getFile();
		final FileWriter fileWriter = new FileWriter(file);
		final CsvMapWriter csvWriter = new CsvMapWriter(fileWriter, preferences);
		try {
			csvWriter.writeHeader(headers);
			for (final Map<String, Object> entry : entries) {
				for (final String element : headers) {
					if (!entry.containsKey(element) || (entry.get(element) == null)) {
						entry.put(element, EMPTY);
					}
				}
				csvWriter.write(entry, headers);
			}
		} finally {
			csvWriter.close();
		}
		return new DataHandler(tmp);
	}

}
