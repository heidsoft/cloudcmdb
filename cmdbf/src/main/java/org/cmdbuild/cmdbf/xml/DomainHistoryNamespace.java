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
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DomainHistoryNamespace extends EntryNamespace {

	public DomainHistoryNamespace(final String name, final DataAccessLogic systemDataAccessLogic,
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

			final Set<String> imports = new HashSet<String>();
			schema = new XmlSchema(getNamespaceURI(), schemaCollection);
			schema.setId(getSystemId());
			schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
			for (final CMDomainHistory domain : getTypes(CMDomainHistory.class)) {
				final XmlSchemaType type = getXsd(domain, document, schema, imports);
				final XmlSchemaElement element = new XmlSchemaElement(schema, true);
				element.setSchemaTypeName(type.getQName());
				element.setName(type.getName());
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
	public Iterable<? extends CMDomainHistory> getTypes(final Class<?> cls) {

		if (CMDomain.class.isAssignableFrom(cls)) {
			return Iterables.transform(
					Iterables.filter(systemDataAccessLogic.findActiveDomains(), new Predicate<CMDomain>() {
						@Override
						public boolean apply(final CMDomain input) {
							return !input.isSystem() && input.getClass1() != null && !input.getClass1().isSystem()
									&& input.getClass2() != null && !input.getClass2().isSystem()
									&& input.holdsHistory();
						}
					}), new Function<Object, CMDomainHistory>() {
						@Override
						public CMDomainHistory apply(final Object input) {
							return input instanceof CMDomain ? new CMDomainHistory((CMDomain) input) : null;
						}
					});
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public QName getTypeQName(final Object type) {
		QName qname = null;
		if (type instanceof CMDomainHistory) {
			final CMEntryType entryType = (CMEntryType) type;
			qname = new QName(getNamespaceURI(), entryType.getName(), getNamespacePrefix());
		}
		return qname;
	}

	@Override
	public CMDomainHistory getType(final QName qname) {
		CMDomainHistory type = null;
		if (getNamespaceURI().equals(qname.getNamespaceURI())) {
			final CMDomain domain = Iterables.tryFind(systemDataAccessLogic.findActiveDomains(),
					new Predicate<CMDomain>() {
						@Override
						public boolean apply(final CMDomain input) {
							return !input.isSystem() && input.getClass1() != null && !input.getClass1().isSystem()
									&& input.getClass2() != null && !input.getClass2().isSystem()
									&& input.holdsHistory();
						}
					}).orNull();
			if (domain != null) {
				type = new CMDomainHistory(domain);
			}
		}
		return type;
	}

	@Override
	public boolean serialize(final Node xml, final Object entry) {
		boolean serialized = false;
		if (entry instanceof CMRelationHistory) {
			final CMRelationHistory relation = (CMRelationHistory) entry;
			serialized = serialize(xml, relation.getType(), relation.getValues());
		}
		return serialized;
	}

	private XmlSchemaType getXsd(final CMDomainHistory domain, final Document document, final XmlSchema schema,
			final Set<String> imports) {
		final XmlSchemaComplexType type = new XmlSchemaComplexType(schema, true);
		type.setName(domain.getName());
		final XmlSchemaComplexContent content = new XmlSchemaComplexContent();
		final XmlSchemaComplexContentExtension extension = new XmlSchemaComplexContentExtension();
		final QName baseTypeName = getRegistry().getTypeQName(domain.getBaseType());
		extension.setBaseTypeName(baseTypeName);
		content.setContent(extension);
		type.setContentModel(content);
		return type;
	}
}
