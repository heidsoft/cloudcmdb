package org.cmdbuild.servlets.json.translationtable.objects.csv;

import java.io.IOException;

import javax.activation.DataHandler;

public interface CsvExporter {

	DataHandler write() throws IOException;

}
