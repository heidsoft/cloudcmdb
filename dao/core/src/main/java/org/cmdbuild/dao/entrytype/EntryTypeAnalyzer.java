package org.cmdbuild.dao.entrytype;

import static com.google.common.base.Predicates.or;
import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.BASE_PROCESS_CLASS_NAME;
import static org.cmdbuild.dao.entrytype.Predicates.attributeTypeInstanceOf;

import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Predicate;

public class EntryTypeAnalyzer {

	public static EntryTypeAnalyzer inspect(final CMEntryType entryType, final Predicate<CMAttribute> predicate,
			final CMDataView view) {
		Validate.notNull(entryType);
		Validate.notNull(predicate);
		Validate.notNull(view);
		return new EntryTypeAnalyzer(entryType, predicate, view);
	}

	private static final Iterable<CMAttribute> NO_ATTRIBUTES = Collections.emptyList();

	private final CMEntryType entryType;
	private final Predicate<CMAttribute> predicate;
	private final CMDataView view;

	private EntryTypeAnalyzer(final CMEntryType entryType, final Predicate<CMAttribute> predicate, final CMDataView view) {
		this.entryType = entryType;
		this.predicate = predicate;
		this.view = view;
	}

	/**
	 * 
	 * @return true if the entry type has at least one ACTIVE and NOT SYSTEM
	 *         attribute of one of the following types: Lookup, Reference,
	 *         ForeignKey; false otherwise
	 */
	public boolean hasExternalReferences() {
		return !from(attributes()) //
				.filter(or( //
						attributeTypeInstanceOf(LookupAttributeType.class), //
						attributeTypeInstanceOf(ReferenceAttributeType.class), //
						attributeTypeInstanceOf(ForeignKeyAttributeType.class) //
				)) //
				.isEmpty();
	}

	public Iterable<CMAttribute> getLookupAttributes() {
		return from(attributes()) //
				.filter(attributeTypeInstanceOf(LookupAttributeType.class));
	}

	public Iterable<CMAttribute> getReferenceAttributes() {
		return from(attributes()) //
				.filter(attributeTypeInstanceOf(ReferenceAttributeType.class));
	}

	public Iterable<CMAttribute> getForeignKeyAttributes() {
		return entryType.holdsHistory() ? NO_ATTRIBUTES : from(attributes()) //
				.filter(attributeTypeInstanceOf(ForeignKeyAttributeType.class));
	}

	private Iterable<CMAttribute> attributes() {
		return from(entryType.getActiveAttributes()) //
				.filter(CMAttribute.class) //
				.filter(predicate);
	}

	public boolean isSimpleClass() {
		final CMClass baseClass = view.findClass(BASE_CLASS_NAME);
		return !baseClass.isAncestorOf((CMClass) entryType);
	}

	public boolean isStandardClass() {
		final CMClass baseClass = view.findClass(BASE_CLASS_NAME);
		return baseClass.isAncestorOf((CMClass) entryType);
	}

	public boolean isProcessClass() {
		final CMClass baseProcessClass = view.findClass(BASE_PROCESS_CLASS_NAME);
		return baseProcessClass.isAncestorOf((CMClass) entryType);
	}

}
