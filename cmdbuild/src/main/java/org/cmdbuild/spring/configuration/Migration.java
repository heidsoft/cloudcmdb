package org.cmdbuild.spring.configuration;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.sql.DataSource;

import org.cmdbuild.services.DefaultPatchManager;
import org.cmdbuild.services.DefaultPatchManager.Repository;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.FilesStoreRepository;
import org.cmdbuild.services.PatchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Migration {

	private static final String PATCHES_DIRECTORY = "patches";
	private static final String PATCHES_EXT_DIRECTORY = "patches-ext";

	@Autowired
	private Data data;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Files fileStore;

	@Bean
	public PatchManager patchManager() {
		return new DefaultPatchManager(dataSource, data.systemDataView(), data.dataDefinitionLogic(), repositories());
	}

	@Bean
	protected Iterable<Repository> repositories() {
		return concat(Arrays.<Repository> asList(patches()), patchesExt());
	}

	@Bean
	protected Repository patches() {
		return new FilesStoreRepository(fileStore.webInfFilesStore().sub(PATCHES_DIRECTORY), null);
	}

	@Bean
	protected Iterable<Repository> patchesExt() {
		final Collection<Repository> output = newArrayList();
		final FilesStore patchesExt = fileStore.webInfFilesStore().sub(PATCHES_EXT_DIRECTORY);
		for (final File element : patchesExt.files(null)) {
			if (element.isDirectory()) {
				final String name = element.getName();
				output.add(new FilesStoreRepository(patchesExt.sub(name), name));
			}
		}
		return output;
	}

}
