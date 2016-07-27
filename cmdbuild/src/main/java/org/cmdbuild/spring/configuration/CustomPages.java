package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.custompage.CustomPagesStore;
import org.cmdbuild.data.store.custompage.CustomPagesStore.Synchronizer;
import org.cmdbuild.data.store.custompage.DBCustomPage;
import org.cmdbuild.data.store.custompage.DBCustomPageConverter;
import org.cmdbuild.data.store.custompage.FileSystemSynchronizer;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.custompages.CustomPage;
import org.cmdbuild.logic.custompages.CustomPageConverter;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.logic.custompages.DefaultConverter;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic.AccessControlHelper;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic.Converter;
import org.cmdbuild.logic.custompages.PrivilegeContextAccessControlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomPages {

	@Autowired
	private Data data;

	@Autowired
	private Files fileStore;

	@Autowired
	private UserStore userStore;

	@Bean
	public CustomPagesLogic defaultCustomPagesLogic() {
		return new DefaultCustomPagesLogic(customPagesStore(), defaultConverter(),
				alwaysAccessibleAccessControlHelper());
	}

	@Bean
	protected Store<DBCustomPage> customPagesStore() {
		return new CustomPagesStore(dataViewStore(), fileSystemSynchronizer());
	}

	@Bean
	protected Synchronizer fileSystemSynchronizer() {
		return new FileSystemSynchronizer(dataViewStore(), fileStore.uploadFilesStore());
	}

	@Bean
	protected Store<DBCustomPage> dataViewStore() {
		return DataViewStore.<DBCustomPage> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(dbCustomPageConverter()) //
				.build();
	}

	@Bean
	protected StorableConverter<DBCustomPage> dbCustomPageConverter() {
		return new DBCustomPageConverter();
	}

	@Bean
	protected Converter defaultConverter() {
		return new DefaultConverter(customPageConverter());
	}

	@Bean
	protected com.google.common.base.Converter<CustomPage, DBCustomPage> customPageConverter() {
		return new CustomPageConverter();
	}

	@Bean
	protected AccessControlHelper alwaysAccessibleAccessControlHelper() {
		return new PrivilegeContextAccessControlHelper(userStore);
	}

}
