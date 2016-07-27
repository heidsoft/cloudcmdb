package org.cmdbuild.data.store.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.cmdbuild.data.store.lookup.Functions.toLookupId;
import static org.cmdbuild.data.store.lookup.Functions.toLookupType;
import static org.cmdbuild.data.store.lookup.Predicates.lookupWithType;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.data.store.ForwardingStore;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DataViewLookupStore extends ForwardingStore<Lookup> implements LookupStore {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewLookupStore.class.getName());

	@SuppressWarnings("unchecked")
	private static final Store<Lookup> unsupported = newProxy(Store.class, unsupported("method not supported"));

	private final Store<Lookup> delegate;

	public DataViewLookupStore(final Store<Lookup> delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Store<Lookup> delegate() {
		return delegate;
	}

	@Override
	public void delete(final Storable storable) {
		unsupported.delete(storable);
	}

	@Override
	public Iterable<Lookup> readAll(final LookupType type) {
		logger.debug(marker, "getting lookups with type '{}'", type);
		final Iterable<Lookup> elements = readAll();
		return from(elements) //
				.filter(lookupWithType(type)) //
				.transform(toLookupWithParent(elements));
	}

	private static Function<Lookup, Lookup> toLookupWithParent(final Iterable<Lookup> elements) {
		return new Function<Lookup, Lookup>() {

			private final Map<Long, Lookup> lookupsById = uniqueIndex(elements, toLookupId());

			@Override
			public Lookup apply(final Lookup input) {
				final Lookup output;
				final Lookup parent = lookupsById.get(input.parentId());
				if (parent != null) {
					output = new ForwardingLookup() {

						@Override
						protected Lookup delegate() {
							return input;
						}

						@Override
						public Lookup parent() {
							return apply(parent);
						}

					};
				} else {
					output = input;
				}
				return output;
			}

		};
	}

	@Override
	public Iterable<LookupType> readAllTypes() {
		return from(readAll()) //
				.transform(toLookupType()) //
				.toSet();
	}

	@Override
	public Iterable<Lookup> readFromUuid(final String uuid) {
		return Iterables.filter(readAll(), new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return ObjectUtils.equals(input.getTranslationUuid(), uuid);
			}
		});
	}

}
