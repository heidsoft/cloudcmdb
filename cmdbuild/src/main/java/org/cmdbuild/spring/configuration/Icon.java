package org.cmdbuild.spring.configuration;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.data.store.icon.IconStorableConverter;
import org.cmdbuild.logic.icon.DefaultConverter;
import org.cmdbuild.logic.icon.DefaultIconsLogic;
import org.cmdbuild.logic.icon.IconsLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Converter;

@Configuration
public class Icon {

	@Autowired
	private Data data;

	@Autowired
	private Files files;

	@Bean
	public IconsLogic defaultIconsLogic() {
		return new DefaultIconsLogic(store(), iconToStorableIconConverter());
	}

	@Bean
	protected Store<org.cmdbuild.data.store.icon.Icon> store() {
		return DataViewStore.<org.cmdbuild.data.store.icon.Icon> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(iconStorableConverter()) //
				.build();
	}

	@Bean
	protected StorableConverter<org.cmdbuild.data.store.icon.Icon> iconStorableConverter() {
		return new IconStorableConverter();
	}

	@Bean
	protected Converter<org.cmdbuild.logic.icon.Icon, org.cmdbuild.data.store.icon.Icon> iconToStorableIconConverter() {
		return new DefaultConverter(files.imagesFileStore());
	}

}
