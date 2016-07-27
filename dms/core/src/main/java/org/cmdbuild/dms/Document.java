package org.cmdbuild.dms;

import java.util.List;

public interface Document {

	String getClassName();

	Long getCardId();

	List<String> getPath();

}
