package org.cmdbuild.dao.query.clause;

import static com.google.common.collect.Iterables.transform;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingClass;

import com.google.common.base.Function;

public class ClassHistory extends ForwardingClass implements HistoricEntryType<CMClass> {

	public static CMClass history(final CMClass current) {
		return of(current);
	}

	public static CMClass of(final CMClass current) {
		return new ClassHistory(current);
	}

	private static final Function<CMClass, CMClass> TO_HISTORIC = new Function<CMClass, CMClass>() {

		@Override
		public CMClass apply(final CMClass input) {
			return history(input);
		}

	};

	private static final CMClass UNSUPPORTED = UnsupportedProxyFactory.of(CMClass.class).create();

	private final CMClass current;
	private final transient String toString;

	private ClassHistory(final CMClass current) {
		this.current = current;
		this.toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("name", current.getIdentifier().getLocalName()) //
				.append("namespace", current.getIdentifier().getNameSpace()) //
				.toString();
	}

	@Override
	protected CMClass delegate() {
		return UNSUPPORTED;
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public CMClass getType() {
		return current;
	}

	@Override
	public String getPrivilegeId() {
		return current.getPrivilegeId();
	}

	@Override
	public CMIdentifier getIdentifier() {
		return current.getIdentifier();
	}

	@Override
	public Long getId() {
		return current.getId();
	}

	@Override
	public String getName() {
		return current.getName() + " HISTORY";
	}

	@Override
	public boolean isSystem() {
		return current.isSystem();
	}

	@Override
	public boolean isSystemButUsable() {
		return current.isSystemButUsable();
	}

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		return transform(current.getLeaves(), TO_HISTORIC);
	}

	@Override
	public Iterable<? extends CMClass> getDescendants() {
		return transform(current.getDescendants(), TO_HISTORIC);
	}

	@Override
	public boolean isAncestorOf(final CMClass cmClass) {
		final CMClass target = cmClass instanceof ClassHistory ? ClassHistory.class.cast(cmClass).current : cmClass;
		return current.isAncestorOf(target);
	}

	@Override
	public boolean isSuperclass() {
		return false;
	}

	@Override
	public Iterable<? extends CMAttribute> getActiveAttributes() {
		return current.getActiveAttributes();
	}

	@Override
	public CMAttribute getAttribute(final String name) {
		return current.getAttribute(name);
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		return current.getAllAttributes();
	}

	@Override
	public boolean holdsHistory() {
		return current.holdsHistory();
	}

	@Override
	public boolean isActive() {
		return current.isActive();
	}

	@Override
	public int hashCode() {
		return current.getId().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CMEntryType == false) {
			return false;
		}
		final CMEntryType other = CMEntryType.class.cast(obj);
		return current.getId().equals(other.getId());
	}

	@Override
	public String toString() {
		return toString;
	}

}
