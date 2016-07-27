package org.cmdbuild.logic.files;

import java.util.concurrent.ExecutionException;

import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachedHashing implements Hashing, Cacheable {

	private static final Logger logger = FileLogic.logger;
	private static final Marker MARKER = MarkerFactory.getMarker(CachedHashing.class.getName());

	private final Hashing delegate;
	private final LoadingCache<String, String> cache;

	public CachedHashing(final Hashing delegate, final CacheExpiration expiration) {
		this.delegate = delegate;
		this.cache = CacheBuilder.newBuilder() //
				.expireAfterWrite(expiration.duration(), expiration.unit()) //
				.build(new CacheLoader<String, String>() {

					@Override
					public String load(final String key) throws Exception {
						return delegate.hash(key);
					}

				});
	}

	@Override
	public String hash(final String value) {
		try {
			logger.info(MARKER, "getting cached value for '{}'", value);
			return cache.get(value);
		} catch (final ExecutionException e) {
			logger.error(MARKER, "error getting cached value", e);
			return delegate.hash(value);
		}
	}

	@Override
	public void clearCache() {
		logger.info(MARKER, "clearing cache");
		cache.invalidateAll();
	}

}
