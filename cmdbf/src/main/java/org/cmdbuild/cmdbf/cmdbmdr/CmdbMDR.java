package org.cmdbuild.cmdbf.cmdbmdr;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.DomainHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;
import static org.cmdbuild.logic.data.lookup.LookupLogic.UNUSED_LOOKUP_QUERY;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.cmdbf.CMDBfId;
import org.cmdbuild.cmdbf.CMDBfItem;
import org.cmdbuild.cmdbf.CMDBfQueryResult;
import org.cmdbuild.cmdbf.CMDBfRelationship;
import org.cmdbuild.cmdbf.CMDBfUtils;
import org.cmdbuild.cmdbf.ContentSelectorFunction;
import org.cmdbuild.cmdbf.ItemSet;
import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.cmdbuild.cmdbf.PathSet;
import org.cmdbuild.cmdbf.xml.CMCardHistory;
import org.cmdbuild.cmdbf.xml.CMClassHistory;
import org.cmdbuild.cmdbf.xml.CMDomainHistory;
import org.cmdbuild.cmdbf.xml.CMRelationHistory;
import org.cmdbuild.cmdbf.xml.DmsDocument;
import org.cmdbuild.cmdbf.xml.GeoCard;
import org.cmdbuild.cmdbf.xml.GeoClass;
import org.cmdbuild.cmdbf.xml.XmlRegistry;
import org.cmdbuild.common.Constants;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.cql.sqlbuilder.attribute.CMFakeAttribute;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.AnyClass;
import org.cmdbuild.dao.query.clause.ClassHistory;
import org.cmdbuild.dao.query.clause.DomainHistory;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.Aliases;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.services.gis.GeoFeatureStore;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.DeregistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidMDRFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidRecordFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.UnsupportedRecordTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.AcceptedType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ComparisonOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeclinedType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterInstanceResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EqualOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NullOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.PropertyValueType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QNameType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType.RecordMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterInstanceResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.StringOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ObjectFactory;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.PropertyValueOperatorsType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryCapabilities;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypeList;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypes;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RegistrationServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ServiceDescription;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.XPathType;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.postgis.Geometry;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CmdbMDR implements ManagementDataRepository {

	private static final Alias TARGET_ALIAS = Aliases.name("TARGET");
	private static final Alias DOMAIN_ALIAS = Aliases.name("DOMAIN");
	private static final String HISTORY_CURRENT_ID = "CurrentId";
	private static final String ENTRY_RECORDID_PREFIX = "entry:";
	private static final String DOCUMENT_RECORDID_PREFIX = "doc:";
	private static final String GEO_RECORDID_PREFIX = "geo:";
	private static final QName LAST_MODIFIED = new QName("http://schemas.dmtf.org/cmdbf/1/tns/serviceData",
			"lastModified");
	private static final QName END_DATE = new QName("http://www.cmdbuild.org/cmdbf/1/tns/serviceData", "endDate");

	private final XmlRegistry xmlRegistry;
	private final MdrScopedIdRegistry aliasRegistry;
	private final LookupLogic lookupLogic;
	private final DataAccessLogic dataAccessLogic;
	private final DmsLogic dmsLogic;
	private final GISLogic gisLogic;
	private final GeoFeatureStore geoFeatureStore;
	private final OperationUser operationUser;
	private final CmdbfConfiguration cmdbfConfiguration;
	private final DmsConfiguration dmsConfiguration;
	private final DatabaseConfiguration databaseConfiguration;
	private final PatchManager patchManager;

	private class CmdbQueryResult extends CMDBfQueryResult {

		public CmdbQueryResult(final QueryType body) throws QueryErrorFault {
			super(body);
			execute();
		}

		@Override
		protected Collection<CMDBfItem> getItems(final String templateId, final Set<CMDBfId> instanceId,
				final RecordConstraintType recordConstraint) {
			try {
				return CmdbMDR.this.getItems(instanceId, recordConstraint);
			} catch (final Exception e) {
				throw new Error(e);
			}
		}

		@Override
		protected Collection<CMDBfRelationship> getRelationships(final String templateId,
				final Set<CMDBfId> instanceId, final ItemSet<CMDBfItem> source, final ItemSet<CMDBfItem> target,
				final RecordConstraintType recordConstraint) throws QueryErrorFault {
			try {
				return CmdbMDR.this.getRelationships(instanceId, source, target, recordConstraint);
			} catch (final Exception e) {
				throw new QueryErrorFault(e.getMessage(), e);
			}
		}

		@Override
		protected void fetchItemRecords(final String templateId, final ItemSet<CMDBfItem> items,
				final ContentSelectorType contentSelector) {
			CmdbMDR.this.fetchItemRecords(items, contentSelector);
		}

		@Override
		protected void fetchRelationshipRecords(final String templateId, final PathSet relationships,
				final ContentSelectorType contentSelector) throws QueryErrorFault {
			try {
				CmdbMDR.this.fetchRelationshipRecords(relationships, contentSelector);
			} catch (final Exception e) {
				throw new QueryErrorFault(e.getMessage(), e);
			}
		}
	}
	
	private class FilterCMAttributeTypeVisitor implements CMAttributeTypeVisitor {
		private Object newValue;
		private Object value;
		
		public FilterCMAttributeTypeVisitor(Object value) {
			this.value = value;
		}
		
		public Object getNewValue() {
			return newValue;
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
			if (newValue != null) {
				newValue = ((DateTime) newValue).toDate();
			}
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			newValue = attributeType.convertValue(value);			
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			Long lookupId = null;
			LookupType lookupType = lookupLogic.typeFor(attributeType.getLookupTypeName());
			for (final Lookup lookup : lookupLogic.getAllLookup(lookupType, true, UNUSED_LOOKUP_QUERY)) {
				if (lookup.description() != null && ObjectUtils.equals(lookup.description(), value)) {
					lookupId = lookup.getId();
				}
			}
			if(lookupId != null)
				newValue = new LookupValue(lookupId, (String)value, attributeType.getLookupTypeName(), null);
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final IntegerAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final DoubleAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
			if (newValue != null) {
				newValue = ((DateTime) newValue).toDate();
			}
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
			if (newValue != null) {
				newValue = ((DateTime) newValue).toDate();
			}
		}

		@Override
		public void visit(final EntryTypeAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final BooleanAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final StringArrayAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}

		@Override
		public void visit(final CharAttributeType attributeType) {
			newValue = attributeType.convertValue(value);
		}
	}

	public CmdbMDR(final XmlRegistry xmlRegistry, final LookupLogic lookupLogic, final DataAccessLogic dataAccessLogic,
			final DmsLogic dmsLogic, final GISLogic gisLogic, final GeoFeatureStore geoFeatureStore,
			final OperationUser operationUser, final MdrScopedIdRegistry aliasRegistry,
			final CmdbfConfiguration cmdbfConfiguration, final DmsConfiguration dmsConfiguration,
			final DatabaseConfiguration databaseConfiguration, final PatchManager patchManager) {
		this.xmlRegistry = xmlRegistry;
		this.lookupLogic = lookupLogic;
		this.dataAccessLogic = dataAccessLogic;
		this.dmsLogic = dmsLogic;
		this.gisLogic = gisLogic;
		this.geoFeatureStore = geoFeatureStore;
		this.operationUser = operationUser;
		this.aliasRegistry = aliasRegistry;
		this.cmdbfConfiguration = cmdbfConfiguration;
		this.dmsConfiguration = dmsConfiguration;
		this.databaseConfiguration = databaseConfiguration;
		this.patchManager = patchManager;
	}

	@Override
	public String getMdrId() {
		return cmdbfConfiguration.getMdrId();
	}

	@Override
	public QueryServiceMetadata getQueryServiceMetadata() {
		final ObjectFactory factory = new ObjectFactory();
		final QueryServiceMetadata queryServiceMetadata = factory.createQueryServiceMetadata();
		queryServiceMetadata.setServiceDescription(getServiceDescription(factory));
		queryServiceMetadata.setRecordTypeList(getRecordTypesList(factory));
		queryServiceMetadata.setQueryCapabilities(getQueryCapabilities(factory));
		return queryServiceMetadata;
	}

	@Override
	public RegistrationServiceMetadata getRegistrationServiceMetadata() {
		final ObjectFactory factory = new ObjectFactory();
		final RegistrationServiceMetadata registrationServiceMetadata = factory.createRegistrationServiceMetadata();
		registrationServiceMetadata.setServiceDescription(getServiceDescription(factory));
		registrationServiceMetadata.setRecordTypeList(getRecordTypesList(factory));
		return registrationServiceMetadata;
	}

	@Override
	public QueryResultType graphQuery(final QueryType body) throws InvalidPropertyTypeFault, UnknownTemplateIDFault,
			ExpensiveQueryErrorFault, QueryErrorFault, XPathErrorFault, UnsupportedSelectorFault,
			UnsupportedConstraintFault {
		return new CmdbQueryResult(body);
	}

	@Override
	public RegisterResponseType register(final RegisterRequestType body) throws UnsupportedRecordTypeFault,
			InvalidRecordFault, InvalidMDRFault, RegistrationErrorFault {
		if (getMdrId().equals(body.getMdrId())) {
			final ItemSet<CMDBfItem> itemSet = new ItemSet<CMDBfItem>();
			final RegisterResponseType registerResponse = new RegisterResponseType();
			final Map<CMDBfItem, RegisterInstanceResponseType> retryList = new HashMap<CMDBfItem, RegisterInstanceResponseType>();
			if (body.getItemList() != null) {
				for (final ItemType item : body.getItemList().getItem()) {
					final CMDBfItem cmdbfItem = new CMDBfItem(item);

					final RegisterInstanceResponseType registerInstanceResponse = new RegisterInstanceResponseType();
					if (!item.getInstanceId().isEmpty()) {
						final MdrScopedIdType instanceId = item.getInstanceId().get(0);
						registerInstanceResponse.setInstanceId(instanceId);
					}
					try {
						if (registerItem(cmdbfItem)) {
							final AcceptedType accepted = new AcceptedType();
							for (final CMDBfId id : cmdbfItem.instanceIds()) {
								accepted.getAlternateInstanceId().add(id);
							}
							registerInstanceResponse.setAccepted(accepted);
						} else {
							retryList.put(cmdbfItem, registerInstanceResponse);
						}
						itemSet.add(cmdbfItem);
					} catch (final Throwable e) {
						Log.CMDBUILD.error("CMDBf register", e);
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(e.getClass().getName() + ": " + cause.getMessage());
							cause = cause.getCause();
						}
						registerInstanceResponse.setDeclined(declined);
					}
					registerResponse.getRegisterInstanceResponse().add(registerInstanceResponse);
				}
			}
			if (body.getRelationshipList() != null) {
				for (final RelationshipType relationship : body.getRelationshipList().getRelationship()) {
					final CMDBfRelationship cmdbfRelationship = new CMDBfRelationship(relationship);
					final CMDBfItem source = itemSet.get(cmdbfRelationship.getSource());
					if (source != null) {
						CMDBfId sourceId = null;
						for (final CMDBfId id : source.instanceIds()) {
							if (aliasRegistry.isLocal(id)) {
								sourceId = id;
							}
						}
						if (sourceId != null) {
							cmdbfRelationship.setSource(sourceId);
						}
					}
					final CMDBfItem target = itemSet.get(cmdbfRelationship.getTarget());
					if (target != null) {
						CMDBfId targetId = null;
						for (final CMDBfId id : target.instanceIds()) {
							if (aliasRegistry.isLocal(id)) {
								targetId = id;
							}
						}
						if (targetId != null) {
							cmdbfRelationship.setTarget(targetId);
						}
					}
					final RegisterInstanceResponseType registerInstanceResponse = new RegisterInstanceResponseType();
					if (!relationship.getInstanceId().isEmpty()) {
						final MdrScopedIdType instanceId = relationship.getInstanceId().get(0);
						registerInstanceResponse.setInstanceId(instanceId);
					}
					try {
						registerRelationship(cmdbfRelationship);
						final AcceptedType accepted = new AcceptedType();
						for (final CMDBfId id : cmdbfRelationship.instanceIds()) {
							accepted.getAlternateInstanceId().add(id);
						}
						registerInstanceResponse.setAccepted(accepted);
					} catch (final Throwable e) {
						Log.CMDBUILD.error("CMDBf register", e);
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(e.getClass().getName() + ": " + cause.getMessage());
							cause = cause.getCause();
						}
						registerInstanceResponse.setDeclined(declined);
					}
					registerResponse.getRegisterInstanceResponse().add(registerInstanceResponse);
				}
			}
			for (final Entry<CMDBfItem, RegisterInstanceResponseType> retryEntry : retryList.entrySet()) {
				final CMDBfItem cmdbfItem = retryEntry.getKey();
				final RegisterInstanceResponseType registerInstanceResponse = retryEntry.getValue();
				try {
					registerItem(cmdbfItem);
					final AcceptedType accepted = new AcceptedType();
					for (final CMDBfId id : cmdbfItem.instanceIds()) {
						accepted.getAlternateInstanceId().add(id);
					}
					registerInstanceResponse.setAccepted(accepted);
				} catch (final Throwable e) {
					Log.CMDBUILD.error("CMDBf register", e);
					final DeclinedType declined = new DeclinedType();
					Throwable cause = e;
					while (cause != null) {
						declined.getReason().add(e.getClass().getName() + ": " + cause.getMessage());
						cause = cause.getCause();
					}
					registerInstanceResponse.setDeclined(declined);
				}
			}
			return registerResponse;
		} else {
			throw new InvalidMDRFault(body.getMdrId());
		}
	}

	@Override
	public DeregisterResponseType deregister(final DeregisterRequestType body) throws DeregistrationErrorFault,
			InvalidMDRFault {
		if (getMdrId().equals(body.getMdrId())) {
			final DeregisterResponseType deregisterResponse = new DeregisterResponseType();
			if (body.getRelationshipIdList() != null) {
				for (final MdrScopedIdType instanceId : body.getRelationshipIdList().getInstanceId()) {
					final DeregisterInstanceResponseType deregisterInstanceResponse = new DeregisterInstanceResponseType();
					deregisterInstanceResponse.setInstanceId(instanceId);
					try {
						deregisterRelationship(instanceId);
					} catch (final Exception e) {
						Log.CMDBUILD.error("CMDBf register", e);
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(e.getClass().getName() + ": " + cause.getMessage());
							cause = cause.getCause();
						}
						deregisterInstanceResponse.setDeclined(declined);
					}
					deregisterResponse.getDeregisterInstanceResponse().add(deregisterInstanceResponse);
				}
			}
			if (body.getItemIdList() != null) {
				for (final MdrScopedIdType instanceId : body.getItemIdList().getInstanceId()) {
					final DeregisterInstanceResponseType deregisterInstanceResponse = new DeregisterInstanceResponseType();
					deregisterInstanceResponse.setInstanceId(instanceId);
					try {
						deregisterItem(instanceId);
					} catch (final Exception e) {
						Log.CMDBUILD.error("CMDBf deregister", e);
						final DeclinedType declined = new DeclinedType();
						Throwable cause = e;
						while (cause != null) {
							declined.getReason().add(e.getClass().getName() + ": " + cause.getMessage());
							cause = cause.getCause();
						}
						deregisterInstanceResponse.setDeclined(declined);
					}
					deregisterResponse.getDeregisterInstanceResponse().add(deregisterInstanceResponse);
				}
			}
			return deregisterResponse;
		} else {
			throw new InvalidMDRFault(body.getMdrId());
		}
	}

	private ServiceDescription getServiceDescription(final ObjectFactory factory) {
		final ServiceDescription serviceDescription = factory.createServiceDescription();
		serviceDescription.setMdrId(getMdrId());
		return serviceDescription;
	}

	private QueryCapabilities getQueryCapabilities(final ObjectFactory factory) {
		final QueryCapabilities queryCapabilities = factory.createQueryCapabilities();

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ContentSelectorType contentSelectorType = factory
				.createContentSelectorType();
		contentSelectorType.setPropertySelector(true);
		contentSelectorType.setRecordTypeSelector(true);
		queryCapabilities.setContentSelectorSupport(contentSelectorType);

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordConstraintType recordConstraintType = factory
				.createRecordConstraintType();
		recordConstraintType.setRecordTypeConstraint(true);
		recordConstraintType.setPropertyValueConstraint(true);
		final PropertyValueOperatorsType propertyValueOperatorsType = factory.createPropertyValueOperatorsType();
		propertyValueOperatorsType.setContains(true);
		propertyValueOperatorsType.setEqual(true);
		propertyValueOperatorsType.setGreater(true);
		propertyValueOperatorsType.setGreaterOrEqual(true);
		propertyValueOperatorsType.setIsNull(true);
		propertyValueOperatorsType.setLess(true);
		propertyValueOperatorsType.setLessOrEqual(true);
		propertyValueOperatorsType.setLike(true);
		recordConstraintType.setPropertyValueOperators(propertyValueOperatorsType);
		queryCapabilities.setRecordConstraintSupport(recordConstraintType);

		final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RelationshipTemplateType relationshipTemplateType = factory
				.createRelationshipTemplateType();
		relationshipTemplateType.setDepthLimit(true);
		relationshipTemplateType.setMinimumMaximum(true);
		queryCapabilities.setRelationshipTemplateSupport(relationshipTemplateType);

		final XPathType xPathType = factory.createXPathType();
		queryCapabilities.setXpathSupport(xPathType);
		return queryCapabilities;
	}

	@SuppressWarnings("unchecked")
	private RecordTypeList getRecordTypesList(final ObjectFactory factory) {
		final Map<String, RecordTypes> recordTypesMap = new HashMap<String, RecordTypes>();
		if (databaseConfiguration.isConfigured() && patchManager.isUpdated()) {
			for (final Object type : Iterables.concat(xmlRegistry.getTypes(CMClass.class),
					xmlRegistry.getTypes(CMClassHistory.class), xmlRegistry.getTypes(CMDomain.class),
					xmlRegistry.getTypes(CMDomainHistory.class), xmlRegistry.getTypes(DocumentTypeDefinition.class),
					xmlRegistry.getTypes(GeoClass.class))) {
				final QName typeQName = xmlRegistry.getTypeQName(type);
				final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordType recordType = factory.createRecordType();
				recordType.setLocalName(typeQName.getLocalPart());
				if (type instanceof CMClass) {
					final CMClass cmClass = (CMClass) type;
					recordType.setAppliesTo("item");
					CMClass parent = null;
					if (type instanceof CMClassHistory) {
						parent = ((CMClassHistory) cmClass).getBaseType();
					} else {
						parent = cmClass.getParent();
					}
					if (parent != null) {
						final QName parentQName = xmlRegistry.getTypeQName(parent);
						final org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QNameType qName = factory.createQNameType();
						qName.setNamespace(parentQName.getNamespaceURI());
						qName.setLocalName(parentQName.getLocalPart());
						recordType.getSuperType().add(qName);
					}
				} else if (type instanceof CMDomain) {
					recordType.setAppliesTo("relationship");
				} else if (type instanceof DocumentTypeDefinition) {
					recordType.setAppliesTo("item");
				} else if (type instanceof GeoClass) {
					recordType.setAppliesTo("item");
				}

				RecordTypes recordTypes = recordTypesMap.get(typeQName.getNamespaceURI());
				if (recordTypes == null) {
					recordTypes = new RecordTypes();
					recordTypes.setNamespace(typeQName.getNamespaceURI());
					recordTypes.setSchemaLocation(xmlRegistry.getByNamespaceURI(typeQName.getNamespaceURI())
							.getSchemaLocation());
					recordTypesMap.put(typeQName.getNamespaceURI(), recordTypes);
				}
				recordTypes.getRecordType().add(recordType);
			}
		}
		final RecordTypeList recordTypeList = factory.createRecordTypeList();
		recordTypeList.getRecordTypes().addAll(recordTypesMap.values());
		return recordTypeList;
	}

	private boolean registerItem(final CMDBfItem item) throws Exception {

		CMCard cmCard = null;
		for (final CMDBfId cmdbfId : item.instanceIds()) {
			if (aliasRegistry.isLocal(cmdbfId)) {
				final List<Long> idList = Collections.singletonList(aliasRegistry.getInstanceId(cmdbfId));
				final CMClass type = dataAccessLogic.findClass(aliasRegistry.getInstanceType(cmdbfId));
				cmCard = Iterables.getOnlyElement(findCards(idList, type, null, null), null);
				if (cmCard == null) {
					for (final CMCard element : findCards(idList, new CMClassHistory(type), null, null)) {
						if (cmCard == null || cmCard.getBeginDate().isBefore(element.getEndDate())) {
							cmCard = element;
						}
					}
				}
			}
		}
		if (cmCard == null) {
			cmCard = resolveItemAlias(item);
		}

		boolean retry = false;
		CMClass cmType = cmCard != null ? cmCard.getType() : null;
		;
		Card.Builder cardBuilder = null;
		DateTime recordLastModified = null;
		for (final RecordType record : item.records()) {
			final QName recordQName = CMDBfUtils.getRecordType(record);
			if (recordQName != null) {
				final Object recordType = xmlRegistry.getType(recordQName);
				if (recordType instanceof CMClass) {
					if (cardBuilder == null) {
						cardBuilder = Card.newInstance();
					}
					if (cmType == null || cmType.isAncestorOf((CMClass) recordType)) {
						cmType = (CMClass) recordType;
						cardBuilder.withClassName(cmType.getName());
					} else if (!(cmType.equals(recordType) || ((CMClass) recordType).isAncestorOf(cmType))) {
						throw new UnsupportedRecordTypeFault("Incompatible record type " + recordQName);
					}
					final Element xml = CMDBfUtils.getRecordContent(record);
					final Card newCard = (Card) xmlRegistry.deserialize(xml);
					cardBuilder.withAllAttributes(newCard.getAttributes());
					if (record.getRecordMetadata() != null && record.getRecordMetadata().getLastModified() != null) {
						final DateTime lastModified = new DateTime(record.getRecordMetadata().getLastModified()
								.toGregorianCalendar().getTimeInMillis());
						if (recordLastModified == null || lastModified.isBefore(recordLastModified)) {
							cardBuilder.withBeginDate(lastModified);
							recordLastModified = lastModified;
						}
					}
				} else if (!(recordType instanceof DocumentTypeDefinition || recordType instanceof GeoClass)) {
					throw new UnsupportedRecordTypeFault("Unsupported record type " + recordQName);
				}
			}
		}

		if (cmType == null) {
			cmType = anyClass();
		}

		Card card = null;
		if (cmCard != null) {
			card = CardStorableConverter.of(cmCard).convert(cmCard);
		}

		if (cardBuilder != null) {
			Card newCard = cardBuilder.build();

			final CMClass newCardType = dataAccessLogic.findClass(newCard.getClassName());
			for (final CMAttribute attribute : newCardType.getActiveAttributes()) {
				if (attribute.getType() instanceof ForeignKeyAttributeType
						|| attribute.getType() instanceof ReferenceAttributeType) {
					final Object value = newCard.getAttribute(attribute.getName());
					if (value instanceof IdAndDescription) {
						retry |= ((IdAndDescription) value).getId() == null;
					}
				}
			}

			if (card == null) {
				final Long id = dataAccessLogic.createCard(newCard);
				item.instanceIds().add(aliasRegistry.getCMDBfId(id, cmType.getName()));
				cardBuilder.withId(id);
				card = cardBuilder.build();
			} else {
				if (card.getEndDate() == null) {
					cardBuilder.withId(card.getId());
					newCard = cardBuilder.build();

					boolean modified = false;
					for (final String key : newCard.getAttributes().keySet()) {
						Object newVal = newCard.getAttribute(key);
						final Object oldVal = card.getAttribute(key);
						if (newVal instanceof String && ((String) newVal).isEmpty()) {
							newVal = null;
						}
						if (newVal != null) {
							modified |= !newVal.equals(oldVal);
						} else {
							modified |= oldVal != null;
						}
					}
					if (modified) {
						final DateTime cardDate = card.getBeginDate();
						final DateTime newCardDate = newCard.getBeginDate();
						if (cardDate == null || newCardDate == null || !newCardDate.isBefore(cardDate)) {
							dataAccessLogic.updateCard(newCard);
						} else {
							throw new RegistrationErrorFault("Out of date");
						}
					}
				} else {
					throw new RegistrationErrorFault("Deleted");
				}
			}
		}

		if (card != null) {
			item.instanceIds().add(aliasRegistry.getCMDBfId(card.getId(), cmType.getName()));
			item.instanceIds().addAll(
					aliasRegistry.getItemAlias(card.getId(), cmType.getName(), card.getAttributes().entrySet()));
		}

		for (final RecordType record : item.records()) {
			final QName recordQName = CMDBfUtils.getRecordType(record);
			final Object recordType = (recordQName != null) ? xmlRegistry.getType(recordQName) : null;
			DateTime recordDate = null;
			if (record.getRecordMetadata() != null && record.getRecordMetadata().getLastModified() != null) {
				recordDate = new DateTime(record.getRecordMetadata().getLastModified().toGregorianCalendar()
						.getTimeInMillis());
			}
			if (card != null && card.getEndDate() == null) {
				if (recordType instanceof DocumentTypeDefinition) {
					final Element xml = CMDBfUtils.getRecordContent(record);
					final DmsDocument newDocument = (DmsDocument) xmlRegistry.deserialize(xml);

					DateTime documentDate = null;
					if (recordDate != null) {
						final List<StoredDocument> documents = dmsLogic.search(card.getClassName(), card.getId());
						final Iterator<StoredDocument> documentIterator = documents.iterator();
						while (recordDate == null && documentIterator.hasNext()) {
							final StoredDocument document = documentIterator.next();
							if (document.getName().equals(newDocument.getName())) {
								documentDate = new DateTime(document.getModified().getTime());
							}
						}
					}
					if (recordDate == null || documentDate == null || !recordDate.isBefore(documentDate)) {
						if (newDocument.getInputStream() != null) {
							dmsLogic.upload(operationUser.getAuthenticatedUser().getUsername(), card.getClassName(),
									card.getId(), newDocument.getInputStream(), newDocument.getName(),
									newDocument.getCategory(), newDocument.getDescription(),
									newDocument.getMetadataGroups());
						} else {
							dmsLogic.updateDescriptionAndMetadata(operationUser.getAuthenticatedUser().getUsername(),
									card.getClassName(), card.getId(), newDocument.getName(), null,
									newDocument.getDescription(), newDocument.getMetadataGroups());
						}
					} else {
						throw new RegistrationErrorFault("Record " + recordQName + " Out of date");
					}
				} else if (recordType instanceof GeoClass) {
					final Element xml = CMDBfUtils.getRecordContent(record);
					final GeoCard geoCard = (GeoCard) xmlRegistry.deserialize(xml);
					final JSONObject jsonObject = new JSONObject();
					for (final LayerMetadata layer : geoCard.getType().getLayers()) {
						final Geometry value = geoCard.get(layer.getName());
						if (value != null) {
							jsonObject.put(layer.getName(), value.toString());
						}
					}
					gisLogic.updateFeatures(card,
							Collections.<String, Object> singletonMap("geoAttributes", jsonObject.toString()));
				}
			} else {
				throw new RegistrationErrorFault("Card for record " + recordQName + " not found");
			}
		}
		return !retry;
	}

	private void registerRelationship(final CMDBfRelationship relationship) throws Exception {

		CmdbRelation relation = null;
		for (final CMDBfId cmdbfId : relationship.instanceIds()) {
			if (aliasRegistry.isLocal(cmdbfId)) {
				final List<Long> idList = Collections.singletonList(aliasRegistry.getInstanceId(cmdbfId));
				final CMDomain domain = dataAccessLogic.findDomain(aliasRegistry.getInstanceType(cmdbfId));
				relation = Iterables.getOnlyElement(findRelations(idList, null, null, domain, null, null), null);
				if (relation == null) {
					for (final CmdbRelation element : findRelations(idList, null, null, domain, null, null)) {
						if (relation == null || relation.getBeginDate().isBefore(element.getEndDate())) {
							relation = element;
						}
					}
				}
			}
		}
		if (relation == null) {
			relation = resolveRelationshipAlias(relationship);
		}

		CMDomain cmType = relation != null ? relation.getType() : null;
		RelationDTO newRelation = null;
		DateTime recordLastModified = null;
		for (final RecordType record : relationship.records()) {
			final QName recordQName = CMDBfUtils.getRecordType(record);
			if (recordQName != null) {
				final Object recordType = xmlRegistry.getType(recordQName);
				if (recordType instanceof CMDomain) {
					if (cmType == null) {
						cmType = (CMDomain) recordType;
					} else if (!(cmType.equals(recordType))) {
						throw new UnsupportedRecordTypeFault("Incompatible record type " + recordQName);
					}

					final Element xml = CMDBfUtils.getRecordContent(record);
					final RelationDTO recordRelation = (RelationDTO) xmlRegistry.deserialize(xml);
					if (newRelation == null) {
						newRelation = recordRelation;
						newRelation.domainName = cmType.getName();
					} else {
						newRelation.relationAttributeToValue.putAll(recordRelation.relationAttributeToValue);
					}

					newRelation.domainName = cmType.getName();

					if (record.getRecordMetadata() != null && record.getRecordMetadata().getLastModified() != null) {
						final DateTime lastModified = new DateTime(record.getRecordMetadata().getLastModified()
								.toGregorianCalendar().getTimeInMillis());
						if (recordLastModified == null || lastModified.isBefore(recordLastModified)) {
							recordLastModified = lastModified;
						}
					}
				} else {
					throw new UnsupportedRecordTypeFault("Unsupported record type " + recordQName);
				}
			}
		}

		final MdrScopedIdType sourceId = relationship.getSource() != null ? getLocalItemId(relationship.getSource())
				: null;
		final MdrScopedIdType targetId = relationship.getTarget() != null ? getLocalItemId(relationship.getTarget())
				: null;

		if (relation == null && sourceId != null && targetId != null && cmType != null) {
			relation = Iterables.getOnlyElement(
					findRelations(null, Collections.singleton(aliasRegistry.getInstanceId(sourceId)),
							Collections.singleton(aliasRegistry.getInstanceId(targetId)), cmType, null, null), null);
		}

		Long relationId = null;
		if (newRelation != null) {
			if (relation == null) {
				if (sourceId != null && targetId != null) {
					newRelation.addSourceCard(aliasRegistry.getInstanceId(sourceId),
							aliasRegistry.getInstanceType(sourceId));
					newRelation.addDestinationCard(aliasRegistry.getInstanceId(targetId),
							aliasRegistry.getInstanceType(targetId));
					relationId = Iterables.getOnlyElement(dataAccessLogic.createRelations(newRelation));
					relationship.instanceIds().add(aliasRegistry.getCMDBfId(relationId, cmType.getName()));
				}
			} else {
				if (relation.getEndDate() == null) {
					boolean modified = false;
					for (final String key : newRelation.relationAttributeToValue.keySet()) {
						Object newVal = newRelation.relationAttributeToValue.get(key);
						final Object oldVal = relation.get(key);
						if (newVal instanceof String && ((String) newVal).isEmpty()) {
							newVal = null;
						}
						if (newVal != null) {
							modified |= !newVal.equals(oldVal);
						} else {
							modified |= oldVal != null;
						}
					}
					relationId = relation.getId();
					if (modified) {
						final DateTime relationDate = relation.getBeginDate();
						if (relationDate == null || recordLastModified == null
								|| !recordLastModified.isBefore(relationDate)) {
							newRelation.relationId = relation.getId();
							newRelation.addSourceCard(relation.getCard1Id(), relation.getType().getClass1()
									.getIdentifier().getLocalName());
							newRelation.addDestinationCard(relation.getCard2Id(), relation.getType().getClass2()
									.getIdentifier().getLocalName());
							dataAccessLogic.updateRelation(newRelation);
						} else {
							throw new RegistrationErrorFault("Out of date");
						}
					}
				} else {
					throw new RegistrationErrorFault("Deleted");
				}
			}
		}
		if (relationId != null) {
			relationship.instanceIds().add(aliasRegistry.getCMDBfId(relationId, cmType.getName()));
			relationship.instanceIds().addAll(
					aliasRegistry.getRelationshipAlias(relationId, cmType.getName(),
							newRelation.relationAttributeToValue.entrySet()));
		}
	}

	private void deregisterItem(final MdrScopedIdType instanceId) throws Exception {
		CMCard card = null;
		if (aliasRegistry.isLocal(instanceId)) {
			final List<Long> idList = Collections.singletonList(aliasRegistry.getInstanceId(instanceId));
			final CMClass type = dataAccessLogic.findClass(aliasRegistry.getInstanceType(instanceId));
			card = Iterables.getOnlyElement(findCards(idList, type, null, null), null);
		} else {
			card = aliasRegistry.resolveItemAlias(instanceId);
		}
		if (card != null) {
			final String recordId = aliasRegistry.getRecordId(instanceId);
			if (recordId == null || recordId.startsWith(ENTRY_RECORDID_PREFIX)) {
				final QName qname = xmlRegistry
						.getTypeQName(new GeoClass(card.getType().getIdentifier().getLocalName()));
				final GeoClass geoClass = (GeoClass) xmlRegistry.getType(qname);
				if (geoClass != null) {
					final JSONObject jsonObject = new JSONObject();
					for (final LayerMetadata layer : geoClass.getLayers()) {
						jsonObject.put(layer.getName(), "");
					}
					gisLogic.updateFeatures(Card.newInstance(card.getType()).withId(card.getId()).build(),
							Collections.<String, Object> singletonMap("geoAttributes", jsonObject.toString()));
				}
				dataAccessLogic.deleteCard(card.getType().getIdentifier().getLocalName(), card.getId());
			} else if (recordId.startsWith(DOCUMENT_RECORDID_PREFIX)) {
				final String name = recordId.substring(DOCUMENT_RECORDID_PREFIX.length());
				dmsLogic.delete(card.getType().getIdentifier().getLocalName(), card.getId(), name);
			} else if (recordId.startsWith(GEO_RECORDID_PREFIX)) {
				final QName qname = xmlRegistry
						.getTypeQName(new GeoClass(card.getType().getIdentifier().getLocalName()));
				final GeoClass geoClass = (GeoClass) xmlRegistry.getType(qname);
				final JSONObject jsonObject = new JSONObject();
				for (final LayerMetadata layer : geoClass.getLayers()) {
					jsonObject.put(layer.getName(), "");
				}
				gisLogic.updateFeatures(Card.newInstance(card.getType()).withId(card.getId()).build(),
						Collections.<String, Object> singletonMap("geoAttributes", jsonObject.toString()));
			}
		} else {
			throw new DeregistrationErrorFault("Not found");
		}
	}

	private void deregisterRelationship(final MdrScopedIdType instanceId) throws Exception {
		CMRelation relation = null;
		if (aliasRegistry.isLocal(instanceId)) {
			final List<Long> idList = Collections.singletonList(aliasRegistry.getInstanceId(instanceId));
			final CMDomain domain = dataAccessLogic.findDomain(aliasRegistry.getInstanceType(instanceId));
			relation = Iterables.getOnlyElement(findRelations(idList, null, null, domain, null, null), null);
		} else {
			relation = aliasRegistry.resolveRelationshipAlias(instanceId);
		}

		if (relation != null) {
			dataAccessLogic.deleteRelation(relation.getType().getName(), relation.getId());
		} else {
			throw new DeregistrationErrorFault("Not found");
		}
	}

	private Collection<CMDBfItem> getItems(final Set<CMDBfId> instanceId, final RecordConstraintType recordConstraint)
			throws Exception {
		try {
			final List<CMClass> typeList = new ArrayList<CMClass>();
			final Map<String, GeoClass> geoTypes = new HashMap<String, GeoClass>();
			final Set<String> documentTypes = new HashSet<String>();
			if (recordConstraint != null) {
				for (final QNameType recordType : recordConstraint.getRecordType()) {
					final Object type = xmlRegistry.getType(new QName(recordType.getNamespace(), recordType
							.getLocalName()));
					if (type instanceof CMClass) {
						typeList.add((CMClass) type);
					} else if (type instanceof DocumentTypeDefinition) {
						documentTypes.add(((DocumentTypeDefinition) type).getName());
					} else if (type instanceof GeoClass) {
						final GeoClass geoClass = (GeoClass) type;
						geoTypes.put(geoClass.getName(), geoClass);
					}
				}
			}
			if (recordConstraint == null || recordConstraint.getRecordType().isEmpty()
					|| (typeList.isEmpty() && !documentTypes.isEmpty())) {
				for (final CMClass cmClass : dataAccessLogic.findActiveClasses()) {
					if (!cmClass.isSystem() && !cmClass.isSuperclass()) {
						typeList.add(cmClass);
					}
				}
			}

			final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Map<String, List<Long>> idMap = buildTypeMap(instanceId, true);
			final List<CMDBfItem> instanceList = new ArrayList<CMDBfItem>();
			final Set<Long> dedupSet = new HashSet<Long>();
			for (final CMClass type : typeList) {
				List<Long> idList = null;
				if (idMap != null) {
					for (final String typeName : idMap.keySet()) {
						final CMClass constraintType = dataAccessLogic.findClass(typeName);
						if (type.isAncestorOf(constraintType)) {
							if (idList == null) {
								idList = new ArrayList<Long>();
							}
							idList.addAll(idMap.get(typeName));
						}
					}
				}
				if (idMap == null || idList != null) {
					for (final CMCard card : findCards(idList, type,
							recordConstraint != null ? recordConstraint.getPropertyValue() : null, null)) {
						if (dedupSet.add(card.getId())) {
							boolean match = true;
							if (match && !documentTypes.isEmpty()) {
								match = false;
								for (final StoredDocument doc : dmsLogic.search(card.getType().getIdentifier()
										.getLocalName(), card.getId())) {
									match |= documentTypes.contains(doc.getCategory());
									if (match && !recordConstraint.getPropertyValue().isEmpty()) {
										final RecordType record = getRecord(aliasRegistry.getCMDBfId(card), doc, null,
												xml);
										final Map<QName, String> properties = CMDBfUtils.parseRecord(record);
										match &= Iterables.all(recordConstraint.getPropertyValue(),
												new Predicate<PropertyValueType>() {
													@Override
													public boolean apply(final PropertyValueType input) {
														return CMDBfUtils.filter(properties, input);
													}
												});
									}
								}
							}
							if (match && !geoTypes.isEmpty()) {
								match = false;
								final GeoClass geoClass = geoTypes.get(type.getIdentifier().getLocalName());
								if (geoClass != null) {
									final RecordType record = getRecord(aliasRegistry.getCMDBfId(card), card.getType(),
											card.getId(), geoClass, xml);
									match = record != null;
									if (match && !recordConstraint.getPropertyValue().isEmpty()) {
										final Map<QName, String> properties = CMDBfUtils.parseRecord(record);
										match &= Iterables.all(recordConstraint.getPropertyValue(),
												new Predicate<PropertyValueType>() {
													@Override
													public boolean apply(final PropertyValueType input) {
														return CMDBfUtils.filter(properties, input);
													}
												});
									}
								}
							}
							if (match) {
								instanceList.add(getCMDBfItem(card));
							}
						}
					}
				}
			}

			return instanceList;
		} catch (final ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	private Collection<CMDBfRelationship> getRelationships(final Set<CMDBfId> instanceId,
			final ItemSet<CMDBfItem> source, final ItemSet<CMDBfItem> target,
			final RecordConstraintType recordConstraint) throws Exception {
		final List<CMDomain> domainList = new ArrayList<CMDomain>();
		if (recordConstraint == null || recordConstraint.getRecordType().isEmpty()) {
			for (final CMDomain domain : dataAccessLogic.findActiveDomains()) {
				if (!domain.isSystem()) {
					domainList.add(domain);
				}
			}
		} else {
			for (final QNameType recordType : recordConstraint.getRecordType()) {
				final Object type = xmlRegistry
						.getType(new QName(recordType.getNamespace(), recordType.getLocalName()));
				if (type instanceof CMDomain) {
					domainList.add((CMDomain) type);
				}
			}
		}

		final Map<String, List<Long>> idMap = buildTypeMap(instanceId, true);
		final List<CMDBfRelationship> relationshipList = new ArrayList<CMDBfRelationship>();
		final Set<Long> dedupSet = new HashSet<Long>();
		for (final CMDomain type : domainList) {
			List<Long> idList = null;
			if (idMap != null) {
				idList = idMap.get(type.getName());
			}
			if (idMap == null || idList != null) {
				for (final CmdbRelation relation : findRelations(idList, buildCardIdList(source),
						buildCardIdList(target), type, recordConstraint != null ? recordConstraint.getPropertyValue()
								: null, null)) {
					if (dedupSet.add(relation.getId())) {
						relationshipList.add(getCMDBfRelationship(relation));
					}
				}
			}
		}
		return relationshipList;
	}

	private void fetchItemRecords(final ItemSet<CMDBfItem> items, final ContentSelectorType contentSelector) {
		try {
			final Map<String, List<Long>> idMap = buildTypeMap(items.idSet(), false);

			Map<QName, Set<QName>> propertyMap = null;
			if (contentSelector != null) {
				propertyMap = CMDBfUtils.parseContentSelector(contentSelector);
			}

			final Set<String> documentTypes = new HashSet<String>();
			final HashMap<String, GeoClass> geoTypes = new HashMap<String, GeoClass>();
			if (propertyMap != null) {
				for (final QName qname : propertyMap.keySet()) {
					if (qname.getNamespaceURI() != null) {
						final Object type = xmlRegistry.getType(qname);
						if (type instanceof DocumentTypeDefinition) {
							documentTypes.add(((DocumentTypeDefinition) type).getName());
						} else if (type instanceof GeoClass) {
							final GeoClass geoClass = (GeoClass) type;
							geoTypes.put(geoClass.getName(), geoClass);
						}
					}
				}
			}

			final ContentSelectorFunction contentSelectorFunction = new ContentSelectorFunction(contentSelector);
			final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			for (final String typeName : idMap.keySet()) {
				if (typeName != null) {
					final CMClass type = dataAccessLogic.findClass(typeName);

					final Collection<QName> properties = getTypeProperties(type, propertyMap, false);
					if (propertyMap == null || properties != null) {
						for (final CMCard card : findCards(idMap.get(typeName), type, null, properties)) {
							final CMDBfItem item = items.get(aliasRegistry.getCMDBfId(card));
							item.records().add(getRecord(card, xml));
						}
					}

					if (propertyMap != null) {
						final CMClassHistory history = new CMClassHistory(type);
						final Collection<QName> historyProperties = getTypeProperties(history, propertyMap, true);
						if (historyProperties != null) {
							for (final CMCard card : findCards(idMap.get(typeName), history, null, historyProperties)) {
								final CMDBfItem item = items.get(aliasRegistry.getCMDBfId(card));
								item.records().add(getRecord(card, xml));
							}
						}
					}

					if (dmsConfiguration.isEnabled()) {
						if (!documentTypes.isEmpty()) {
							for (final Long cardId : idMap.get(typeName)) {
								for (final StoredDocument document : dmsLogic.search(type.getIdentifier()
										.getLocalName(), cardId)) {
									if (documentTypes.contains(document.getCategory())) {
										final DataHandler dataHandler = dmsLogic.download(type.getIdentifier()
												.getLocalName(), cardId, document.getName());
										final CMDBfId id = aliasRegistry.getCMDBfId(cardId, type.getName());
										final CMDBfItem item = items.get(id);
										final RecordType record = getRecord(id, document, dataHandler.getInputStream(),
												xml);
										item.records().add(contentSelectorFunction.apply(record));
									}
								}
							}
						}
					}

					if (gisLogic.isGisEnabled()) {
						final GeoClass geoClass = geoTypes.get(type.getIdentifier().getLocalName());
						if (geoClass != null) {
							for (final Long cardId : idMap.get(typeName)) {
								final CMDBfId id = aliasRegistry.getCMDBfId(cardId, type.getName());
								final CMDBfItem item = items.get(id);
								final RecordType record = getRecord(id, type, cardId, geoClass, xml);
								if (record != null) {
									item.records().add(contentSelectorFunction.apply(record));
								}
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			throw new Error(e);
		}
	}

	private void fetchRelationshipRecords(final PathSet relationships, final ContentSelectorType contentSelector)
			throws Exception {
		try {
			final Map<String, List<Long>> idMap = buildTypeMap(relationships.idSet(), false);

			Map<QName, Set<QName>> propertyMap = null;
			if (contentSelector != null) {
				propertyMap = CMDBfUtils.parseContentSelector(contentSelector);
			}
			final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			for (final String typeName : idMap.keySet()) {
				if (typeName != null) {
					final CMDomain type = dataAccessLogic.findDomain(typeName);
					final Collection<QName> properties = getTypeProperties(type, propertyMap, false);
					if (propertyMap == null || properties != null) {
						for (final CMRelation relation : findRelations(idMap.get(typeName), null, null, type, null,
								properties)) {
							final CMDBfItem item = relationships.get(aliasRegistry.getCMDBfId(relation));
							item.records().add(getRecord(relation, xml));
						}
					}

					if (propertyMap != null) {
						final CMDomainHistory history = new CMDomainHistory(type);
						final Collection<QName> historyProperties = getTypeProperties(history, propertyMap, true);
						if (historyProperties != null) {
							for (final CMRelation relation : findRelations(idMap.get(typeName), null, null, history,
									null, historyProperties)) {
								final CMDBfItem item = relationships.get(aliasRegistry.getCMDBfId(relation));
								item.records().add(getRecord(relation, xml));
							}
						}
					}
				}
			}
		} catch (final ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	private Collection<CMCard> findCards(final Collection<Long> instanceId, final CMClass cmClass,
			final Collection<PropertyValueType> filters, final Collection<QName> properties) {
		final List<CMCard> cardList = new ArrayList<CMCard>();
		final List<CMClass> types = new ArrayList<CMClass>();
		if (cmClass instanceof CMClassHistory) {
			final CMClass baseClass = ((CMClassHistory) cmClass).getBaseType();
			if (baseClass instanceof AnyClass) {
				for (final CMClass subType : dataAccessLogic.findActiveClasses()) {
					if (subType.isActive() && !subType.isSuperclass()) {
						types.add(history(subType));
					}
				}
			} else if (baseClass.isSuperclass()) {
				for (final CMClass subType : baseClass.getDescendants()) {
					if (subType.isActive() && !subType.isSuperclass()) {
						types.add(history(subType));
					}
				}
			} else {
				types.add(history(baseClass));
			}
		} else if (cmClass instanceof AnyClass) {
			for (final CMClass subType : dataAccessLogic.findActiveClasses()) {
				if (subType.isActive() && !subType.isSuperclass()) {
					types.add(subType);
				}
			}
		} else {
			types.add(cmClass);
		}

		final Set<Long> instanceIdSet = new HashSet<Long>();
		Iterator<Long> instanceIdIterator = null;
		if (instanceId != null) {
			instanceIdIterator = instanceId.iterator();
		}

		do {
			for (final CMClass type : types) {
				Collection<Long> instanceIdList = null;
				if (instanceIdIterator != null) {
					instanceIdList = new ArrayList<Long>();
					for (int i = 0; i < 1000 && instanceIdIterator.hasNext(); i++) {
						final Long id = instanceIdIterator.next();
						if (instanceIdSet.add(id)) {
							instanceIdList.add(id);
						}
					}
				}

				final List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
				if (properties != null && !properties.contains(new QName(""))) {
					for (final QName property : properties) {
						if (type.getAttribute(property.getLocalPart()) != null) {
							attributes.add(attribute(type, property.getLocalPart()));
						}
					}
				} else {
					attributes.add(anyAttribute(type));
				}

				boolean isSatisfiable = true;
				final List<WhereClause> conditions = new ArrayList<WhereClause>();
				if (instanceIdList != null) {
					if (type instanceof ClassHistory) {
						isSatisfiable = applyIdFilter(attribute(type, HISTORY_CURRENT_ID), instanceIdList, conditions);
					} else {
						isSatisfiable = applyIdFilter(attribute(type, Constants.ID_ATTRIBUTE), instanceIdList,
								conditions);
					}
				}
				if (filters != null) {
					isSatisfiable &= applyPropertyFilter(type, null, filters, conditions);
				}
				if (isSatisfiable) {
					final QuerySpecsBuilder queryBuilder = dataAccessLogic.getView().select(attributes.toArray())
							.from(type);
					if (!conditions.isEmpty()) {
						if (conditions.size() == 1) {
							queryBuilder.where(conditions.get(0));
						} else if (conditions.size() == 2) {
							queryBuilder.where(and(conditions.get(0), conditions.get(1)));
						} else {
							queryBuilder.where(and(conditions.get(0), conditions.get(1),
									conditions.subList(2, conditions.size()).toArray(new WhereClause[0])));
						}
					} else {
						queryBuilder.where(trueWhereClause());
					}
					for (final CMQueryRow row : queryBuilder.run()) {
						CMCard card = row.getCard(type);
						if (card.getEndDate() != null) {
							card = new CMCardHistory(card);
						}
						cardList.add(card);
					}
				}
			}
		} while (instanceIdIterator != null && instanceIdIterator.hasNext());
		return cardList;
	}

	private Collection<CmdbRelation> findRelations(final Collection<Long> instanceId, final Collection<Long> source,
			final Collection<Long> target, final CMDomain type, final Collection<PropertyValueType> filters,
			final Collection<QName> properties) {
		final List<CmdbRelation> relationList = new ArrayList<CmdbRelation>();

		final List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
		if (properties != null && !properties.contains(new QName(""))) {
			for (final QName property : properties) {
				if (type.getAttribute(property.getLocalPart()) != null) {
					attributes.add(attribute(DOMAIN_ALIAS, property.getLocalPart()));
				}
			}
		} else {
			attributes.add(anyAttribute(DOMAIN_ALIAS));
		}

		boolean isSatisfiable = true;
		final List<WhereClause> conditions = new ArrayList<WhereClause>();
		conditions.add(condition(attribute(DOMAIN_ALIAS, SystemAttributes.DomainQuerySource.getDBName()),
				eq(Source._1.name())));

		if (source != null) {
			isSatisfiable = applyIdFilter(attribute(DOMAIN_ALIAS, SystemAttributes.DomainId1.getDBName()), source,
					conditions);
		}
		if (target != null) {
			isSatisfiable = applyIdFilter(attribute(DOMAIN_ALIAS, SystemAttributes.DomainId2.getDBName()), target,
					conditions);
		}
		if (instanceId != null) {
			isSatisfiable &= applyIdFilter(attribute(DOMAIN_ALIAS, Constants.ID_ATTRIBUTE), instanceId, conditions);
		}
		if (filters != null) {
			isSatisfiable &= applyPropertyFilter(type, DOMAIN_ALIAS, filters, conditions);
		}
		if (isSatisfiable) {
			final QuerySpecsBuilder queryBuilder = dataAccessLogic.getView().select(attributes.toArray());
			queryBuilder.from(type.getClass1());
			queryBuilder.join(
					type.getClass2(),
					TARGET_ALIAS,
					over(type instanceof CMDomainHistory ? history(((CMDomainHistory) type).getBaseType()) : type,
							as(DOMAIN_ALIAS)));
			if (!conditions.isEmpty()) {
				if (conditions.size() == 1) {
					queryBuilder.where(conditions.get(0));
				} else if (conditions.size() == 2) {
					queryBuilder.where(and(conditions.get(0), conditions.get(1)));
				} else {
					queryBuilder.where(and(conditions.get(0), conditions.get(1),
							conditions.subList(2, conditions.size()).toArray(new WhereClause[0])));
				}
			} else {
				queryBuilder.where(trueWhereClause());
			}
			for (final CMQueryRow row : queryBuilder.run()) {
				final CMCard sourceCard = row.getCard(type.getClass1());
				final CMCard targetCard = row.getCard(TARGET_ALIAS);
				final CMRelation relation = row.getRelation(DOMAIN_ALIAS).getRelation();
				if (!(type instanceof CMDomainHistory) || relation.getEndDate() != null) {
					if (relation.getEndDate() != null) {
						relationList.add(new CMRelationHistory(relation, sourceCard.getType().getName(), targetCard
								.getType().getName()));
					} else {
						relationList.add(new CmdbRelation(relation, sourceCard.getType().getName(), targetCard
								.getType().getName()));
					}
				}
			}
		}
		return relationList;
	}

	private CMDBfItem getCMDBfItem(final CMCard card) {
		final CMDBfItem item = new CMDBfItem(aliasRegistry.getCMDBfId(card));
		for (final CMDBfId alias : aliasRegistry.getItemAlias(card.getId(), card.getType().getName(), card.getValues())) {
			item.instanceIds().add(alias);
		}
		return item;
	}

	private CMDBfRelationship getCMDBfRelationship(final CmdbRelation relation) {
		final CMDBfRelationship relationship = new CMDBfRelationship(aliasRegistry.getCMDBfId(relation),
				aliasRegistry.getCMDBfId(relation.getCard1Id(), relation.getCard1ClassName()),
				aliasRegistry.getCMDBfId(relation.getCard2Id(), relation.getCard2ClassName()));
		for (final CMDBfId alias : aliasRegistry.getRelationshipAlias(relation.getId(), relation.getType().getName(),
				relation.getValues())) {
			relationship.instanceIds().add(alias);
		}
		return relationship;
	}

	private RecordType getRecord(final CMEntry element, final Document xml) {
		try {
			final DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, element);
			final Element xmlElement = (Element) root.getFirstChild();
			final RecordMetadata recordMetadata = new RecordMetadata();
			final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(element,
					ENTRY_RECORDID_PREFIX + element.getType().getIdentifier().getLocalName()).getLocalId()
					+ (element.getEndDate() != null ? "_" + element.getEndDate().toString() : ""));
			final GregorianCalendar beginDate = element.getBeginDate().toGregorianCalendar();
			recordMetadata.setLastModified(datatypeFactory.newXMLGregorianCalendar(beginDate));
			if (element.getEndDate() != null) {
				final GregorianCalendar endDate = element.getEndDate().toGregorianCalendar();
				final Element endDateElement = xml.createElementNS(END_DATE.getNamespaceURI(), END_DATE.getLocalPart());
				endDateElement.setTextContent(datatypeFactory.newXMLGregorianCalendar(endDate).toXMLFormat());
				recordMetadata.getAny().add(endDateElement);
			}

			final RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} catch (final DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}

	private RecordType getRecord(final CMDBfId id, final StoredDocument document, final InputStream inputStream,
			final Document xml) {
		try {
			final DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, new DmsDocument(document, inputStream));
			final Element xmlElement = (Element) root.getFirstChild();
			final RecordMetadata recordMetadata = new RecordMetadata();
			final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(id, DOCUMENT_RECORDID_PREFIX + document.getName())
					.getLocalId());
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(document.getCreated());
			recordMetadata.setLastModified(datatypeFactory.newXMLGregorianCalendar(calendar));
			final RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} catch (final DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}

	private RecordType getRecord(final CMDBfId id, final CMClass cardType, final Long cardId, final GeoClass geoClass,
			final Document xml) throws Exception {
		final GeoCard geoCard = new GeoCard(geoClass);
		final Card masterCard = Card.newInstance(cardType).withId(cardId).build();
		for (final LayerMetadata layer : geoClass.getLayers()) {
			final GeoFeature feature = geoFeatureStore.readGeoFeature(layer, masterCard);
			if (feature != null) {
				geoCard.set(layer.getName(), feature.getGeometry());
			}
		}
		if (!geoCard.isEmpty()) {
			final DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, geoCard);
			final Element xmlElement = (Element) root.getFirstChild();
			final RecordMetadata recordMetadata = new RecordMetadata();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(id, GEO_RECORDID_PREFIX + geoCard.getType().getName())
					.getLocalId());
			final RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} else {
			return null;
		}
	}

	private boolean applyIdFilter(final QueryAliasAttribute attribute, final Collection<Long> idList,
			final List<WhereClause> conditions) {
		boolean isSatisfiable = true;
		if (idList.isEmpty()) {
			isSatisfiable = false;
		} else {
			conditions.add(condition(attribute, in(idList.toArray())));
		}
		return isSatisfiable;
	}

	private boolean applyPropertyFilter(final CMEntryType type, final Alias alias,
			final Collection<PropertyValueType> propertyValueList, final List<WhereClause> conditions) {
		boolean isSatisfiable = true;
		final Alias typeAlias = alias != null ? alias : Aliases.canonical(type);
		final Iterator<PropertyValueType> iterator = propertyValueList.iterator();
		while (isSatisfiable && iterator.hasNext()) {
			final PropertyValueType propertyValue = iterator.next();
			CMAttribute attribute = null;
			if (propertyValue.isRecordMetadata()) {
				if (type instanceof ClassHistory || type instanceof DomainHistory) {
					if (END_DATE.getNamespaceURI().equals(propertyValue.getNamespace())
							&& END_DATE.getLocalPart().equals(propertyValue.getLocalName())) {
						attribute = new CMFakeAttribute(SystemAttributes.EndDate.getDBName(), type,
								new DateAttributeType(), false);
					}
				}
				if (LAST_MODIFIED.getNamespaceURI().equals(propertyValue.getNamespace())
						&& LAST_MODIFIED.getLocalPart().equals(propertyValue.getLocalName())) {
					attribute = new CMFakeAttribute(SystemAttributes.BeginDate.getDBName(), type,
							new DateAttributeType(), false);

				}
			} else {
				attribute = type.getAttribute(propertyValue.getLocalName());
			}
			if (attribute != null) {
				final List<WhereClause> expressions = new ArrayList<WhereClause>();
				if (propertyValue.getEqual() != null) {
					for (final EqualOperatorType operator : propertyValue.getEqual()) {
						WhereClause filter = condition(attribute(typeAlias, attribute.getName()),
								eq(convertFilterValue(attribute, operator.getValue())));
						if (operator.isNegate()) {
							filter = not(filter);
						}
						expressions.add(filter);
					}
				}
				if (propertyValue.getLess() != null) {
					final ComparisonOperatorType operator = propertyValue.getLess();
					WhereClause filter = condition(attribute(typeAlias, attribute.getName()),
							lt(convertFilterValue(attribute, operator.getValue())));
					if (operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getLessOrEqual() != null) {
					final ComparisonOperatorType operator = propertyValue.getLessOrEqual();
					WhereClause filter = condition(attribute(typeAlias, attribute.getName()),
							gt(convertFilterValue(attribute, operator.getValue())));
					if (!operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getGreater() != null) {
					final ComparisonOperatorType operator = propertyValue.getGreater();
					WhereClause filter = condition(attribute(typeAlias, attribute.getName()),
							gt(convertFilterValue(attribute, operator.getValue())));
					if (operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getGreaterOrEqual() != null) {
					final ComparisonOperatorType operator = propertyValue.getGreaterOrEqual();
					WhereClause filter = condition(attribute(typeAlias, attribute.getName()),
							lt(convertFilterValue(attribute, operator.getValue())));
					if (!operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (propertyValue.getContains() != null) {
					for (final StringOperatorType operator : propertyValue.getContains()) {
						WhereClause filter = condition(attribute(typeAlias, attribute.getName()), contains(attribute
								.getType().convertValue(operator.getValue())));
						if (operator.isNegate()) {
							filter = not(filter);
						}
						expressions.add(filter);
					}
				}
				if (propertyValue.getLike() != null) {
					for (final StringOperatorType operator : propertyValue.getLike()) {
						WhereClause filter = null;
						if (operator.getValue().startsWith("%") && operator.getValue().endsWith("%")) {
							filter = condition(attribute(typeAlias, attribute.getName()), contains(operator.getValue()
									.substring(1, operator.getValue().length() - 1)));
						} else if (operator.getValue().startsWith("%")) {
							filter = condition(attribute(typeAlias, attribute.getName()), endsWith(operator.getValue()
									.substring(1)));
						} else if (operator.getValue().endsWith("%")) {
							filter = condition(attribute(typeAlias, attribute.getName()), beginsWith(operator
									.getValue().substring(0, operator.getValue().length() - 1)));
						} else {
							filter = condition(attribute(typeAlias, attribute.getName()), contains(operator.getValue()));
						}
						if (operator.isNegate()) {
							filter = not(filter);
						}
						expressions.add(filter);
					}
				}
				if (propertyValue.getIsNull() != null) {
					final NullOperatorType operator = propertyValue.getIsNull();
					WhereClause filter = condition(attribute(typeAlias, attribute.getName()), isNull());
					if (operator.isNegate()) {
						filter = not(filter);
					}
					expressions.add(filter);
				}
				if (!expressions.isEmpty()) {
					WhereClause propertyFilter = null;
					if (propertyValue.isMatchAny()) {
						if (expressions.size() == 1) {
							propertyFilter = expressions.get(0);
						} else if (expressions.size() == 2) {
							propertyFilter = or(expressions.get(0), expressions.get(1));
						} else {
							propertyFilter = or(expressions.get(0), expressions.get(1),
									expressions.subList(2, expressions.size()).toArray(new WhereClause[0]));
						}
					} else {
						if (expressions.size() == 1) {
							propertyFilter = expressions.get(0);
						} else if (expressions.size() == 2) {
							propertyFilter = and(expressions.get(0), expressions.get(1));
						} else {
							propertyFilter = and(expressions.get(0), expressions.get(1),
									expressions.subList(2, expressions.size()).toArray(new WhereClause[0]));
						}
					}
					conditions.add(propertyFilter);
				}
			} else {
				isSatisfiable = false;
			}
		}
		return isSatisfiable;
	}

	private Collection<QName> getTypeProperties(CMEntryType type, final Map<QName, Set<QName>> propertyMap,
			final boolean onlyExplicit) {
		Set<QName> properties = null;
		if (propertyMap != null) {
			while (type != null) {
				final Set<QName> propertySet = propertyMap.get(xmlRegistry.getTypeQName(type));
				if (propertySet != null) {
					if (properties == null) {
						properties = new HashSet<QName>();
					}
					for (final QName property : propertySet) {
						properties.add(property);
					}
				}
				if (type instanceof CMClass) {
					type = ((CMClass) type).getParent();
				} else {
					type = null;
				}
			}
			if (!onlyExplicit && propertyMap.containsKey(new QName(""))) {
				if (properties == null) {
					properties = new HashSet<QName>();
				}
				for (final QName property : propertyMap.get(new QName(""))) {
					properties.add(property);
				}
			}

		}
		return properties;
	}

	private Object convertFilterValue(final CMAttribute attribute, final Object value) {
		FilterCMAttributeTypeVisitor visitor = new FilterCMAttributeTypeVisitor(value);
		attribute.getType().accept(visitor);
		return visitor.getNewValue();
	}

	private CMCard resolveItemAlias(final CMDBfItem item) throws Exception {
		CMCard card = null;
		for (final CMDBfId cmdbfId : item.instanceIds()) {
			if (!aliasRegistry.isLocal(cmdbfId)) {
				final CMCard resolved = aliasRegistry.resolveItemAlias(cmdbfId);
				if (card == null) {
					card = resolved;
				} else if (resolved == null || !card.getId().equals(resolved.getId())) {
					throw new IllegalArgumentException("InstanceIds identifies more than one item");
				}
			}
		}
		return card;
	}

	private CmdbRelation resolveRelationshipAlias(final CMDBfRelationship relationship) throws Exception {
		CmdbRelation relation = null;
		for (final CMDBfId cmdbfId : relationship.instanceIds()) {
			if (!aliasRegistry.isLocal(cmdbfId)) {
				final CmdbRelation resolved = aliasRegistry.resolveRelationshipAlias(cmdbfId);
				if (relation == null) {
					relation = resolved;
				} else if (resolved == null || !relation.getId().equals(resolved.getId())) {
					throw new IllegalArgumentException("InstanceIds identifies more than one relationship");
				}
			}
		}
		return relation;
	}

	private MdrScopedIdType getLocalItemId(final MdrScopedIdType instanceId) throws Exception {
		MdrScopedIdType id = null;
		if (aliasRegistry.isLocal(instanceId)) {
			id = instanceId;
		} else {
			final CMCard resolved = aliasRegistry.resolveItemAlias(instanceId);
			if (resolved != null) {
				id = aliasRegistry.getCMDBfId(resolved);
			}
		}
		return id;
	}

	private List<Long> buildCardIdList(final ItemSet<CMDBfItem> items) throws Exception {
		List<Long> idList = null;
		if (items != null) {
			idList = new ArrayList<Long>();
			for (final CMDBfItem item : items) {
				Long cardId = null;
				for (final CMDBfId id : item.instanceIds()) {
					if (aliasRegistry.isLocal(id)) {
						if (cardId == null) {
							cardId = aliasRegistry.getInstanceId(id);
						} else {
							throw new IllegalArgumentException("InstanceIds identifies more than one relationship");
						}
					}
				}
				if (cardId == null) {
					final CMCard card = resolveItemAlias(item);
					if (card != null) {
						cardId = card.getId();
					}
				}
				if (cardId != null) {
					idList.add(cardId);
				}
			}
		}
		return idList;
	}

	private Map<String, List<Long>> buildTypeMap(final Iterable<? extends MdrScopedIdType> instanceId,
			final boolean resolveAlias) throws Exception {
		Map<String, List<Long>> idMap = null;
		if (instanceId != null) {
			idMap = new HashMap<String, List<Long>>();
			for (MdrScopedIdType id : instanceId) {
				if (!aliasRegistry.isLocal(id)) {
					CMEntry entry = aliasRegistry.resolveItemAlias(id);
					if (entry == null) {
						entry = aliasRegistry.resolveRelationshipAlias(id);
					}
					id = (entry != null) ? aliasRegistry.getCMDBfId(entry) : null;
				}
				if (id != null) {
					final String typeName = aliasRegistry.getInstanceType(id);
					if (typeName != null) {
						List<Long> idList = idMap.get(typeName);
						if (idList == null) {
							idList = new ArrayList<Long>();
							idMap.put(typeName, idList);
						}
						idList.add(aliasRegistry.getInstanceId(id));
					}
				}
			}
		}
		return idMap;
	}
}
