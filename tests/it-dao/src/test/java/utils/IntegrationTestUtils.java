package utils;

import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;
import static org.cmdbuild.dao.entrytype.DBIdentifier.fromNameAndNamespace;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.dao.view.DBDataView.DBDomainDefinition;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class IntegrationTestUtils {

	public static final DBClass NO_PARENT = null;

	private IntegrationTestUtils() {
		// prevents instantiation
	}

	public static CMIdentifier withIdentifier(final String localname) {
		return fromName(localname);
	}

	public static CMIdentifier withIdentifier(final String localname, final String namespace) {
		return fromNameAndNamespace(localname, namespace);
	}

	public static DBClassDefinition newSimpleClass(final String name) {
		return newSimpleClass(withIdentifier(name));
	}

	public static DBClassDefinition newSimpleClass(final CMIdentifier identifier) {
		final DBClassDefinition definition = newClass(identifier);
		when(definition.isHoldingHistory()).thenReturn(false);
		return definition;
	}

	public static DBClassDefinition newClass(final String name) {
		return newClass(withIdentifier(name, CMIdentifier.DEFAULT_NAMESPACE), NO_PARENT);
	}

	public static DBClassDefinition newClass(final String name, final DBClass parent) {
		return newClass(withIdentifier(name), parent);
	}

	public static DBClassDefinition newClass(final CMIdentifier identifier) {
		return newClass(identifier, NO_PARENT);
	}

	public static DBClassDefinition newClass(final CMIdentifier identifier, final DBClass parent) {
		final DBClassDefinition definition = mock(DBClassDefinition.class);
		when(definition.getIdentifier()).thenReturn(identifier);
		when(definition.getParent()).thenReturn(parent);
		when(definition.isHoldingHistory()).thenReturn(true);
		when(definition.isActive()).thenReturn(true);
		return definition;
	}

	public static DBClassDefinition newSuperClass(final String name) {
		return newSuperClass(name, NO_PARENT);
	}

	public static DBClassDefinition newSuperClass(final String name, final DBClass parent) {
		final DBClassDefinition definition = newClass(name, parent);
		when(definition.isSuperClass()).thenReturn(true);
		return definition;
	}

	public static DBClassDefinition newSuperClass(final CMIdentifier identifier, final DBClass parent) {
		final DBClassDefinition definition = newClass(identifier, parent);
		when(definition.isSuperClass()).thenReturn(true);
		return definition;
	}

	public static DBDomainDefinition newDomain(final CMIdentifier identifier, final DBClass class1, final DBClass class2) {
		final DBDomainDefinition definition = mock(DBDomainDefinition.class);
		when(definition.getIdentifier()).thenReturn(identifier);
		when(definition.getClass1()).thenReturn(class1);
		when(definition.getClass2()).thenReturn(class2);
		return definition;
	}

	public static DBAttributeDefinition newTextAttribute(final String name, final DBEntryType owner) {
		return newAttribute(name, new TextAttributeType(), owner);
	}

	public static DBAttributeDefinition newAttribute(final String name, final CMAttributeType<?> type,
			final DBEntryType owner) {
		final DBAttributeDefinition definition = mock(DBAttributeDefinition.class);
		when(definition.getName()).thenReturn(name);
		when(definition.getOwner()).thenReturn(owner);
		when(definition.getMode()).thenReturn(CMAttribute.Mode.WRITE);
		when(definition.isActive()).thenReturn(true);
		when(definition.getType()).thenReturn((CMAttributeType) type);
		return definition;
	}

	public static Iterable<String> namesOf(final Iterable<? extends CMEntryType> entityTypes) {
		return Iterables.transform(entityTypes, new Function<CMEntryType, String>() {

			@Override
			public String apply(final CMEntryType input) {
				return input.getName();
			}

		});
	}

	public static QueryAliasAttribute keyAttribute(final CMEntryType et) {
		return attribute(et, et.getKeyAttributeName());
	}

	public static QueryAliasAttribute codeAttribute(final CMClass c) {
		return attribute(c, c.getCodeAttributeName());
	}

	public static QueryAliasAttribute codeAttribute(final Alias alias, final CMClass c) {
		return attribute(alias, c.getCodeAttributeName());
	}

	public static QueryAliasAttribute descriptionAttribute(final CMClass c) {
		return attribute(c, c.getDescriptionAttributeName());
	}

}
