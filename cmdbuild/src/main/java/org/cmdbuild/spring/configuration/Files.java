package org.cmdbuild.spring.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.cmdbuild.common.utils.Reflection.unsupported;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.cmdbuild.logic.files.CachedFileSystemFacade;
import org.cmdbuild.logic.files.CachedHashing;
import org.cmdbuild.logic.files.DefaultFileLogic;
import org.cmdbuild.logic.files.DefaultFileStore;
import org.cmdbuild.logic.files.DefaultFileSystemFacade;
import org.cmdbuild.logic.files.DefaultHashing;
import org.cmdbuild.logic.files.CacheExpiration;
import org.cmdbuild.logic.files.FileLogic;
import org.cmdbuild.logic.files.FileStore;
import org.cmdbuild.logic.files.FileSystemFacade;
import org.cmdbuild.logic.files.Hashing;
import org.cmdbuild.services.DefaultFilesStore;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.ForwardingFilesStore;
import org.cmdbuild.services.Settings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Files {

	private static final String IMAGES_DIRECTORY = "images";

	private static class FilesStoreWithLimitations extends ForwardingFilesStore {

		private static final FilesStore UNSUPPORTED_PRIVATE = newProxy(FilesStore.class,
				unsupported("method not supported"));

		private final FilesStore delegate;

		public FilesStoreWithLimitations(final FilesStore delegate) {
			this.delegate = delegate;
		}

		@Override
		protected FilesStore delegate() {
			return UNSUPPORTED_PRIVATE;
		}

		@Override
		public FilesStore sub(final String dir) {
			return delegate.sub(dir);
		}

		@Override
		public String[] list(final String dir) {
			return delegate.list(dir);
		}

		@Override
		public String[] list(final String dir, final String pattern) {
			return delegate.list(dir, pattern);
		}

		@Override
		public Iterable<File> files(final String dir, final String pattern) {
			return delegate.files(dir, pattern);
		}

		@Override
		public Iterable<File> files(final String pattern) {
			return delegate.files(pattern);
		}

		@Override
		public String getRelativeRootDirectory() {
			return delegate.getRelativeRootDirectory();
		}

		@Override
		public String getAbsoluteRootDirectory() {
			return delegate.getAbsoluteRootDirectory();
		}

	}

	public static final String UPLOAD = "upload";

	@Bean(name = UPLOAD)
	public FilesStore uploadFilesStore() {
		return new DefaultFilesStore(Settings.getInstance().getRootPath(), "upload");
	}

	public static final String ROOT = "root";

	@Bean(name = ROOT)
	public FilesStore webInfFilesStore() {
		return new FilesStoreWithLimitations(_webInfFilesStore());
	}

	@Bean
	protected FilesStore _webInfFilesStore() {
		return new DefaultFilesStore(Settings.getInstance().getRootPath(), "WEB-INF");
	}

	@Bean
	public FileLogic defaultFileLogic() {
		final Map<String, FileStore> map = new HashMap<>();
		map.put(IMAGES_DIRECTORY, imagesFileStore());
		return new DefaultFileLogic(map);
	}

	@Bean
	protected FileStore imagesFileStore() {
		return new DefaultFileStore(cachedFileSystemFacade(), cachedHashing());
	}

	@Bean
	protected CachedFileSystemFacade cachedFileSystemFacade() {
		return new CachedFileSystemFacade(defaultFileSystemFacade(), new CacheExpiration() {

			@Override
			public long duration() {
				return 10;
			}

			@Override
			public TimeUnit unit() {
				return MINUTES;
			}

		});
	}

	@Bean
	protected FileSystemFacade defaultFileSystemFacade() {
		return new DefaultFileSystemFacade(uploadFilesStore().sub(IMAGES_DIRECTORY));
	}

	@Bean
	protected CachedHashing cachedHashing() {
		return new CachedHashing(defaultHashing(), new CacheExpiration() {

			@Override
			public long duration() {
				return 10;
			}

			@Override
			public TimeUnit unit() {
				return MINUTES;
			}

		});
	}

	@Bean
	protected Hashing defaultHashing() {
		return new DefaultHashing();
	}

}
