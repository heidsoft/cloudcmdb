package org.cmdbuild.data.store.custompage;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.data.store.custompage.Functions.name;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.custompage.CustomPagesStore.Synchronizer;
import org.cmdbuild.services.FilesStore;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.MapDifference;

public class FileSystemSynchronizer implements Synchronizer {

	private static final Marker MARKER = MarkerFactory.getMarker(FileSystemSynchronizer.class.getName());

	private static class FileAdapter implements DBCustomPage {

		private final File delegate;

		public FileAdapter(final File delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getIdentifier() {
			return null;
		}

		@Override
		public Long getId() {
			return null;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public String getDescription() {
			return delegate.getName();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof FileAdapter)) {
				return false;
			}
			final FileAdapter other = FileAdapter.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getIdentifier(), other.getIdentifier()) //
					.append(this.getId(), other.getId()) //
					.append(this.getName(), other.getName()) //
					.append(this.getDescription(), other.getDescription()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getIdentifier()) //
					.append(getId()) //
					.append(getName()) //
					.append(getDescription()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static final Predicate<File> DIRECTORY = new Predicate<File>() {

		@Override
		public boolean apply(final File input) {
			logger.debug(MARKER, "file '{}' must be a directory", input);
			return input.isDirectory();
		}

	};

	private static final Function<File, DBCustomPage> FILE_TO_CUSTOM_PAGE = new Function<File, DBCustomPage>() {

		@Override
		public DBCustomPage apply(final File input) {
			logger.debug(MARKER, "converting file '{}' to '{}'", input, DBCustomPage.class);
			return new FileAdapter(input);
		}

	};

	private final Store<DBCustomPage> store;
	private final FilesStore filesStore;

	public FileSystemSynchronizer(final Store<DBCustomPage> store, final FilesStore filesStore) {
		this.store = store;
		this.filesStore = filesStore;
	}

	@Override
	public void synchronize() {
		logger.debug(MARKER, "synchronizing");
		logger.debug(MARKER, "checking cache");
		logger.debug(MARKER, "reading custom pages on file system");
		final Map<String, DBCustomPage> customPagesOnFileSystem = uniqueIndex(
				from(filesStore.files("custompages", null)) //
						.filter(DIRECTORY) //
						.transform(FILE_TO_CUSTOM_PAGE), //
				name());
		logger.debug(MARKER, "reading custom pages from store");
		final Map<String, DBCustomPage> customPagesOnDatabase = uniqueIndex(store.readAll(), name());
		logger.debug(MARKER, "checking differences between collections");
		final MapDifference<String, DBCustomPage> difference = difference(customPagesOnFileSystem,
				customPagesOnDatabase);
		for (final DBCustomPage element : difference.entriesOnlyOnLeft().values()) {
			logger.debug(MARKER, "element '{}' must be created", element);
			store.create(element);
		}
		for (final DBCustomPage element : difference.entriesOnlyOnRight().values()) {
			logger.debug(MARKER, "element '{}' must be deleted", element);
			store.delete(element);
		}
	}

}
