package org.cmdbuild.services.email;

import javax.activation.DataHandler;

public interface Attachment {

	String getName();

	DataHandler getDataHandler();

}