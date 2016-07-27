package org.cmdbuild.services.meta;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.metadata.Metadata;

public interface MetadataStoreFactory {

	Store<Metadata> storeForAttribute(CMAttribute attribute);

}