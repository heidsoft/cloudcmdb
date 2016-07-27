package org.cmdbuild.dao.view.user.privileges;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class PartiallyCachingRowAndColumnPrivilegeFetcher extends ForwardingRowAndColumnPrivilegeFetcher {

	private static class DelegateCacheLoader extends CacheLoader<CMEntryType, Map<String, String>> {

		private final RowAndColumnPrivilegeFetcher delegate;

		public DelegateCacheLoader(final RowAndColumnPrivilegeFetcher delegate) {
			this.delegate = delegate;
		}

		@Override
		public Map<String, String> load(final CMEntryType key) throws Exception {
			return delegate.fetchAttributesPrivilegesFor(key);
		}

	}

	private final RowAndColumnPrivilegeFetcher delegate;
	private final LoadingCache<CMEntryType, Map<String, String>> cache;

	public PartiallyCachingRowAndColumnPrivilegeFetcher(final RowAndColumnPrivilegeFetcher delegate) {
		this.delegate = delegate;
		this.cache = CacheBuilder.newBuilder().build(new DelegateCacheLoader(delegate));
	}

	@Override
	protected RowAndColumnPrivilegeFetcher delegate() {
		return delegate;
	}

	@Override
	public Map<String, String> fetchAttributesPrivilegesFor(final CMEntryType entryType) {
		return cache.getUnchecked(entryType);
	}

}
