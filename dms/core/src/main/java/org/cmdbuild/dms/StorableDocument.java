package org.cmdbuild.dms;

import java.io.InputStream;

public interface StorableDocument extends DocumentUpdate {

	InputStream getInputStream();

}
