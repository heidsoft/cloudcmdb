package org.cmdbuild.cmdbf.xml;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ClassHistoryNamespace extends EntryNamespace {

	public ClassHistoryNamespace(final String name, final DataAccessLogic systemDataAccessLogic,
			final DataAccessLogic userDataAccessLogic, final DataDefinitionLogic dataDefinitionLogic,
			final LookupLogic lookupLogic, final CmdbfConfiguration cmdbfConfiguration) {
		super(name, systemDataAccessLogic, userDataAccessLogic, dataDefinitionLogic, lookupLogic, cmdbfConfiguration);
	}

	@Override
	public XmlSchema getSchema() {

		try {
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
			final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
			XmlSchema schema = null;

			schema = new XmlSchema(getNamespaceURI(), schemaCollection);
			schema.setId(getSystemId());
			schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
			final Set<String> imports = new HashSet<String>();

			for (final CMClassHistory cmClass : getTypes(CMClassHistory.class)) {
				XmlSchemaType type = schema.getTypeByName(cmClass.getName());
				if (type == null) {
					type = getXsd(cmClass, document, schema, imports);
					final XmlSchemaElement element = new XmlSchemaElement(schema, true);
					element.setSchemaTypeName(type.getQName());
					element.setName(type.getName());
				}
			}
			for (final String namespace : imports) {
				final XmlSchemaImport schemaImport = new XmlSchemaImport(schema);
				schemaImport.setNamespace(namespace);
				schemaImport.setSchemaLocation(getRegistry().getByNamespaceURI(namespace).getSchemaLocation());
			}
			return schema;
		} catch (final ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	@Override
	public boolean updateSchema(final XmlSchema schema) {
		return false;
	}

	@Override
	public Iterable<? extends CMClassHistory> getTypes(final Class<?> cls) {
		if (CMClassHistory.class.isAssignableFrom(cls)) {
			return Iterables.transform(
					Iterables.filter(systemDataAccessLogic.findActiveClasses(), new Predicate<CMClass>() {
						@Override
						public boolean apply(final CMClass input) {
							return !input.isSystem() && input.holdsHistory();
						}
					}), new Function<Object, CMClassHistory>() {
						@Override
						public CMClassHistory apply(final Object input) {
							return input instanceof CMClass ? new CMClassHistory((CMClass) input) : null;
						}
					});
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public QName getTypeQName(final Object type) {
		QName qname = null;
		if (type instanceof CMClassHistory) {
			final CMEntryType entryType = (CMEntryType) type;
			qname = new QName(getNamespaceURI(), entryType.getName(), getNamespacePrefix());
		}
		return qname;
	}

	@Override
	public CMClassHistory getType(final QName qname) {
		CMClassHistory type = null;
		if (getNamespaceURI().equals(qname.getNamespaceURI())) {
			final CMClass cmClass = Iterables.tryFind(systemDataAccessLogic.findActiveClasses(),
					new Predicate<CMClass>() {
						@Override
						public boolean apply(final CMClass input) {
							return !input.isSystem() && input.holdsHistory()
									&& input.getName().equals(qname.getLocalPart());
						}
					}).orNull();
			if (cmClass != null) {
				type = new CMClassHistory(cmClass);
			}
		}
		return type;
	}

	@Override
	public boolean serialize(final Node xml, final Object entry) {
		boolean serialized = false;
		if (entry instanceof CMCardHistory) {
			final CMCardHistory card = (CMCardHistory) entry;
			serialized = serialize(xml, card.getType(), card.getValues());
		}
		return serialized;
	}

	private XmlSchemaType getXsd(final CMClassHistory cmClass, final Document document, final XmlSchema schema,
			final Set<String> imports) {
		final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, true);
		type.setName(cmClass.getName());
		final XmlSchemaComplexContent content = new XmlSchemaComplexContent();
		final XmlSchemaComplexContentExtension extension = new XmlSchemaComplexContentExtension();
		final QName baseTypeName = getRegistry().getTypeQName(cmClass.getBaseType());
		extension.setBaseTypeName(baseTypeName);
		content.setContent(extension);
		type.setContentModel(content);
		return type;
	}
}
