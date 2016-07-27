package org.cmdbuild.dms.cmis;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.cmdbuild.dms.MetadataType;

public interface Converter {

	interface Context {

		CmisDmsConfiguration getConfiguration();

		CategoryLookupConverter getCategoryLookupConverter();

	}

	void setContext(Context context);

	Object convertToCmisValue(Session session, PropertyDefinition<?> propertyDefinition, String value);

	String convertFromCmisValue(Session session, PropertyDefinition<?> propertyDefinition, Object value);

	MetadataType getType(PropertyDefinition<?> propertyDefinition);

	boolean isAsymmetric();

}
