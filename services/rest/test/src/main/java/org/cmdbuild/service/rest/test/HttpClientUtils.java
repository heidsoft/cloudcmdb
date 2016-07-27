package org.cmdbuild.service.rest.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;

public class HttpClientUtils {

	public static int statusCodeOf(final HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}

	public static InputStream contentOf(final HttpResponse response) throws IllegalStateException, IOException {
		return response.getEntity().getContent();
	}

	private HttpClientUtils() {
		// prevents instantiation
	}

}
