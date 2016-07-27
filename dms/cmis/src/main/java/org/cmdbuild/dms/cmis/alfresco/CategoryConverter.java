package org.cmdbuild.dms.cmis.alfresco;

import static org.cmdbuild.dms.MetadataType.TEXT;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.cmdbuild.dms.MetadataType;
import org.cmdbuild.dms.cmis.Converter;

public class CategoryConverter implements Converter {

	private Context context;

	@Override
	public void setContext(final Context context) {
		this.context = context;
	}

	@Override
	public boolean isAsymmetric() {
		return true;
	}

	@Override
	public MetadataType getType(final PropertyDefinition<?> propertyDefinition) {
		return TEXT;
	}

	@Override
	public Object convertToCmisValue(final Session session, final PropertyDefinition<?> propertyDefinition,
			final String value) {
		return context.getCategoryLookupConverter().toCmis(value);
	}

	@Override
	public String convertFromCmisValue(final Session cmisSession, final PropertyDefinition<?> propertyDefinition,
			final Object value) {
		return context.getCategoryLookupConverter().fromCmis(value);
	}
}
