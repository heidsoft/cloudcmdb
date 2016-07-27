package org.cmdbuild.dms.cmis;

public interface CategoryLookupConverter {

	Object toCmis(final String value);

	String fromCmis(final Object value);

}
