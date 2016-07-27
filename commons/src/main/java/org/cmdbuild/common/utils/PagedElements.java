package org.cmdbuild.common.utils;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Iterator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PagedElements<T> implements Iterable<T> {

	public static <T> PagedElements<T> empty() {
		final Iterable<T> emptyList = emptyList();
		return new PagedElements<T>(emptyList, 0);
	}

	private final Iterable<T> elements;
	private final int totalSize;

	public PagedElements(final Iterable<T> elements, final int totalSize) {
		this.elements = elements;
		this.totalSize = totalSize;
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	public Iterable<T> elements() {
		return elements;
	}

	public int totalSize() {
		return totalSize;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PagedElements)) {
			return false;
		}
		final PagedElements<T> other = PagedElements.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.elements, other.elements) //
				.append(this.totalSize, other.totalSize) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(elements) //
				.append(totalSize) //
				.toHashCode();
	}

	@Override
	public final String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
