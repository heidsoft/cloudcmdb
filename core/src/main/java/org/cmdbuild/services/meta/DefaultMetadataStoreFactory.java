package org.cmdbuild.services.meta;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.metadata.Metadata;
import org.cmdbuild.data.store.metadata.MetadataConverter;
import org.cmdbuild.data.store.metadata.MetadataGroupable;

public class DefaultMetadataStoreFactory implements MetadataStoreFactory {

	private final CMDataView dataView;

	public DefaultMetadataStoreFactory(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public Store<Metadata> storeForAttribute(final CMAttribute attribute) {
		final MetadataGroupable groupable = MetadataGroupable.of(attribute);
		return DataViewStore.<Metadata> newInstance() //
				.withDataView(dataView) //
				.withGroupable(groupable) //
				.withStorableConverter(MetadataConverter.of(groupable)) //
				.build();
	}

}
