package org.cmdbuild.logic.files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.activation.DataHandler;

import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachedFileSystemFacade extends ForwardingFileSystemFacade implements Cacheable {

	private static final Logger logger = FileLogic.logger;
	private static final Marker MARKER = MarkerFactory.getMarker(CachedFileSystemFacade.class.getName());

	private static final Object DUMMY_KEY = new Object();

	private final FileSystemFacade delegate;
	private final LoadingCache<Object, Collection<File>> cache;

	public CachedFileSystemFacade(final FileSystemFacade delegate, final CacheExpiration expiration) {
		this.delegate = delegate;
		this.cache = CacheBuilder.newBuilder() //
				.expireAfterWrite(expiration.duration(), expiration.unit()) //
				.build(new CacheLoader<Object, Collection<File>>() {

					@Override
					public Collection<File> load(final Object key) throws Exception {
						return delegate.directories();
					}

				});
	}

	@Override
	protected FileSystemFacade delegate() {
		return delegate;
	}

	@Override
	public Collection<File> directories() {
		try {
			logger.info(MARKER, "getting cached values");
			return cache.get(DUMMY_KEY);
		} catch (final ExecutionException e) {
			logger.error(MARKER, "error getting cached values", e);
			return delegate.directories();
		}
	}

	@Override
	public File save(final DataHandler dataHandler, final String path) throws IOException {
		try {
			return super.save(dataHandler, path);
		} finally {
			cache.invalidateAll();
		}
	}

	@Override
	public void clearCache() {
		cache.invalidateAll();
	}

}
