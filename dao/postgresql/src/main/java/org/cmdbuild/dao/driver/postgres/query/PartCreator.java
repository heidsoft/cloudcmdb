package org.cmdbuild.dao.driver.postgres.query;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.driver.postgres.quote.EntryTypeHistoryQuoter;
import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.driver.postgres.quote.ParamAdder;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.ClassHistory;
import org.cmdbuild.dao.query.clause.where.Native;

public abstract class PartCreator {

	protected final StringBuilder sb;
	private final List<Object> params;

	/**
	 * Usable by subclasses only.
	 */
	protected PartCreator() {
		sb = new StringBuilder();
		params = new ArrayList<Object>();
	}

	public final String getPart() {
		return sb.toString();
	}

	protected final String param(final Object o) {
		return param(o, null);
	}

	// TODO Handle CMDBuild and Geographic types conversion
	protected final String param(final Object o, final String cast) {
		final String output;
		if (o instanceof List) {
			final List<Object> l = (List<Object>) o;
			if (l.size() == 1 && l.get(0) instanceof Native) {
				output = Native.class.cast(l.get(0)).expression;
			} else {
				final StringBuilder sb = new StringBuilder("(");
				int i = 1;
				for (final Object value : l) {
					sb.append("?");
					if (i < l.size()) {
						sb.append(",");
						i++;
					}
					Object effectiveValue = value;
					if (value instanceof IdAndDescription) {
						effectiveValue = IdAndDescription.class.cast(value).getId();
					}
					params.add(effectiveValue);
				}
				sb.append(")");
				output = sb.toString();
			}
		} else {
			params.add(o);
			output = "?" + (cast != null ? "::" + cast : "");
		}
		return output;
	}

	public final List<Object> getParams() {
		return params;
	}

	protected final String quoteType(final CMEntryType type) {
		if (type instanceof ClassHistory) {
			return EntryTypeHistoryQuoter.quote(type);
		}
		return EntryTypeQuoter.quote(type, new ParamAdder() {

			@Override
			public void add(final Object value) {
				params.add(value);
			}

		});
	}
}
