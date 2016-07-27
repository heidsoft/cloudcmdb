package org.cmdbuild.servlets.json.serializers;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_VALUE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.EDITOR_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD_MODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.INDEX;
import static org.cmdbuild.servlets.json.CommunicationConstants.INHERITED;
import static org.cmdbuild.servlets.json.CommunicationConstants.IP_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.LENGTH;
import static org.cmdbuild.servlets.json.CommunicationConstants.LOOKUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.META;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOT_NULL;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRECISION;
import static org.cmdbuild.servlets.json.CommunicationConstants.SCALE;
import static org.cmdbuild.servlets.json.CommunicationConstants.SHOW_IN_GRID;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.UNIQUE;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.data.store.metadata.Metadata;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Attribute.IpType;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class AttributeSerializer extends Serializer {

	public static enum JsonModeMapper {

		WRITE("write", Mode.WRITE), //
		READ("read", Mode.READ), //
		HIDDEN("hidden", Mode.HIDDEN), //
		;

		private final String text;
		private final Mode mode;

		private JsonModeMapper(final String text, final Mode mode) {
			this.text = text;
			this.mode = mode;
		}

		public static Mode modeFrom(final String text) {
			for (final JsonModeMapper mapper : values()) {
				if (mapper.text.equals(text)) {
					return mapper.mode;
				}
			}
			return Mode.WRITE;
		}

		public static String textFrom(final Mode mode) {
			for (final JsonModeMapper mapper : values()) {
				if (mapper.mode.equals(mode)) {
					return mapper.text;
				}
			}
			return WRITE.text;
		}

		public String getText() {
			return text;
		}
	}

	public JSONArray toClient(final Iterable<? extends CMAttribute> attributes, final boolean active)
			throws JSONException {
		final JSONArray attributeList = new JSONArray();
		for (final CMAttribute attribute : sortAttributes(attributes)) {
			if (active && !attribute.isActive()) {
				continue;
			}
			attributeList.put(toClient(attribute));
		}
		return attributeList;
	}

	/**
	 * we sort attributes on the class order and index number because Ext.JS
	 * DOES NOT ALLOW IT. Thanks Jack!
	 */
	private static Iterable<? extends CMAttribute> sortAttributes(final Iterable<? extends CMAttribute> attributes) {
		return new Ordering<CMAttribute>() {

			@Override
			public int compare(final CMAttribute left, final CMAttribute right) {
				if (left.getClassOrder() == right.getClassOrder()) {
					return (left.getIndex() - right.getIndex());
				} else {
					return (left.getClassOrder() - right.getClassOrder());
				}
			}
		}.immutableSortedCopy(attributes);
	}

	public JSONObject toClient(final CMAttribute attribute) throws JSONException {
		return toClient(attribute, false);
	}

	public JSONObject toClient(final CMAttribute attribute, final boolean withClassId) throws JSONException {
		final MetadataStoreFactory metadataStoreFactory = applicationContext().getBean(MetadataStoreFactory.class);
		final Store<Metadata> metadataStore = metadataStoreFactory.storeForAttribute(attribute);
		final Map<String, String> metadata = newHashMap();
		for (final Metadata element : metadataStore.readAll()) {
			metadata.put(element.name(), element.value());
		}
		final JSONObject jsonAttribute = toClient(attribute, metadata);
		if (withClassId) {
			jsonAttribute.put("idClass", attribute.getOwner().getId());
		}

		return jsonAttribute;
	}

	public JSONObject toClient(final CMAttribute attribute, final Map<String, String> metadata) throws JSONException {
		final Map<String, Object> serializedAttribute = new SerializerAttributeVisitor(attribute, metadata).serialize();
		return attributesToJsonObject(serializedAttribute);
	}

	private static JSONObject attributesToJsonObject(final Map<String, Object> attributes) throws JSONException {
		final JSONObject jsonObject = new JSONObject();
		for (final Entry<String, Object> entry : attributes.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> nestedAttributes = (Map<String, Object>) value;
				value = attributesToJsonObject(nestedAttributes);
			}
			jsonObject.put(entry.getKey(), value);
		}
		return jsonObject;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeSerializer> {

		CMDataView dataView;

		@Override
		public AttributeSerializer build() {
			return new AttributeSerializer(this);
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

	}

	private final CMDataView view;

	private AttributeSerializer(final Builder builder) {
		this.view = builder.dataView;
	}

	// FIXME: replace List<CMAttributeType<?>> with List<String> with attribute
	// types names
	public static JSONArray toClient(final List<CMAttributeType<?>> types) throws JSONException {
		final JSONArray out = new JSONArray();
		for (final CMAttributeType<?> type : types) {
			final JSONObject jsonType = new CMAttributeTypeVisitor() {

				@Override
				public void visit(final TimeAttributeType attributeType) {
					put("name", "TIME");
					put("value", "TIME");
				}

				@Override
				public void visit(final TextAttributeType attributeType) {
					put("name", "TEXT");
					put("value", "TEXT");
				}

				@Override
				public void visit(final StringAttributeType attributeType) {
					put("name", "STRING");
					put("value", "STRING");
				}

				@Override
				public void visit(final StringArrayAttributeType attributeType) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void visit(final ReferenceAttributeType attributeType) {
					put("name", "REFERENCE");
					put("value", "REFERENCE");
				}

				@Override
				public void visit(final LookupAttributeType attributeType) {
					put("name", "LOOKUP");
					put("value", "LOOKUP");
				}

				@Override
				public void visit(final IpAddressAttributeType attributeType) {
					put("name", "INET");
					put("value", "INET");
				}

				@Override
				public void visit(final IntegerAttributeType attributeType) {
					put("name", "INTEGER");
					put("value", "INTEGER");
				}

				@Override
				public void visit(final ForeignKeyAttributeType attributeType) {
					put("name", "FOREIGNKEY");
					put("value", "FOREIGNKEY");
				}

				@Override
				public void visit(final EntryTypeAttributeType attributeType) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void visit(final DoubleAttributeType attributeType) {
					put("name", "DOUBLE");
					put("value", "DOUBLE");
				}

				@Override
				public void visit(final DecimalAttributeType attributeType) {
					put("name", "DECIMAL");
					put("value", "DECIMAL");
				}

				@Override
				public void visit(final DateTimeAttributeType attributeType) {
					put("name", "TIMESTAMP");
					put("value", "TIMESTAMP");
				}

				@Override
				public void visit(final DateAttributeType attributeType) {
					put("name", "DATE");
					put("value", "DATE");
				}

				@Override
				public void visit(final CharAttributeType attributeType) {
					put("name", "CHAR");
					put("value", "CHAR");
				}

				@Override
				public void visit(final BooleanAttributeType attributeType) {
					put("name", "BOOLEAN");
					put("value", "BOOLEAN");
				}

				private void put(final String key, final String value) {
					try {
						jsonType.put(key, value);
					} catch (final Exception e) {
						throw new Error(e);
					}
				}

				private JSONObject jsonType;

				public JSONObject jsonOf(final CMAttributeType<?> type) {
					jsonType = new JSONObject();
					type.accept(this);
					return jsonType;
				}

			}.jsonOf(type);

			out.put(jsonType);
		}

		return out;
	}

	private class SerializerAttributeVisitor extends ForwardingAttributeTypeVisitor {

		private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

		private final CMAttribute attribute;
		private final Map<String, String> metadata;
		private final Map<String, Object> serialization = Maps.newHashMap();

		private final LookupLogic lookupLogic = applicationContext().getBean(LookupLogic.class);

		private SerializerAttributeVisitor(final CMAttribute attribute, final Map<String, String> metadata) {
			this.attribute = attribute;
			this.metadata = metadata;
		}

		@Override
		protected CMAttributeTypeVisitor delegate() {
			return DELEGATE;
		}

		@Override
		public void visit(final DecimalAttributeType attributeType) {
			serialization.put(PRECISION, attributeType.precision);
			serialization.put(SCALE, attributeType.scale);
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			serialization.put("fkDestination", attributeType.getForeignKeyDestinationClassName());
			serialization.put("filter", attribute.getFilter());
		}

		@Override
		public void visit(final IpAddressAttributeType attributeType) {
			serialization.put(IP_TYPE, IpType.of(attributeType.getType()).name);
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			final String lookupTypeName = attributeType.getLookupTypeName();
			serialization.put(LOOKUP, lookupTypeName);

			final JSONArray lookupChain = new JSONArray();
			LookupType lookupType = lookupLogic.typeFor(lookupTypeName);
			while (lookupType != null) {
				lookupChain.put(lookupType.name);
				lookupType = lookupLogic.typeFor(lookupType.parent);
			}

			serialization.put("lookupchain", lookupChain);
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			final String domainName = attributeType.getDomainName();
			final CMDomain domain = view.findDomain(domainName);
			if (domain == null) {
				throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(domainName);
			}

			final String domainCardinality = domain.getCardinality();
			CMClass target = null;
			if ("N:1".equals(domainCardinality)) {
				target = domain.getClass2();
			} else if ("1:N".equals(domainCardinality)) {
				target = domain.getClass1();
			}

			serialization.put("idClass", target.getId());
			serialization.put("referencedClassName", target.getIdentifier().getLocalName());
			serialization.put("domainName", domain.getIdentifier().getLocalName());
			serialization.put("idDomain", domain.getId());
			serialization.put("filter", attribute.getFilter());
		}

		@Override
		public void visit(final StringAttributeType attributeType) {
			serialization.put(LENGTH, attributeType.length);
		}

		@Override
		public void visit(final TextAttributeType attributeType) {
			serialization.put(EDITOR_TYPE, attribute.getEditorType());
		}

		public Map<String, Object> serialize() {

			/*
			 * type specific
			 */
			attribute.getType().accept(this);

			/*
			 * common
			 */
			serialization.put(NAME, attribute.getName());
			serialization.put(DESCRIPTION, defaultIfBlank(attribute.getDescription(), attribute.getName()));
			serialization.put(TYPE,
					new JsonDashboardDTO.JsonDataSourceParameter.TypeConverter(attribute.getType()).getTypeName());
			serialization.put(SHOW_IN_GRID, attribute.isDisplayableInList());
			serialization.put(UNIQUE, attribute.isUnique());
			serialization.put(NOT_NULL, attribute.isMandatory());
			serialization.put(INHERITED, attribute.isInherited());
			serialization.put(ACTIVE, attribute.isActive());
			serialization.put(FIELD_MODE, JsonModeMapper.textFrom(attribute.getMode()));
			serialization.put(INDEX, attribute.getIndex());
			serialization.put(DEFAULT_VALUE, attribute.getDefaultValue());
			serialization.put(GROUP, defaultIfBlank(attribute.getGroup(), EMPTY));

			final Map<String, String> metadataMap = Maps.newHashMap();
			for (final Entry<String, String> entry : metadata.entrySet()) {
				metadataMap.put(entry.getKey(), entry.getValue());
			}

			serialization.put(META, metadataMap);

			int absoluteClassOrder = attribute.getClassOrder();
			int classOrderSign;
			if (absoluteClassOrder == 0) {
				classOrderSign = 0;
				// to manage the sorting in the AttributeGridForSorting
				absoluteClassOrder = 10000;
			} else if (absoluteClassOrder > 0) {
				classOrderSign = 1;
			} else {
				classOrderSign = -1;
				absoluteClassOrder *= -1;
			}
			serialization.put("classOrderSign", classOrderSign); // TODO
																	// constant
			serialization.put("absoluteClassOrder", absoluteClassOrder); // TODO
																			// constant
			return serialization;
		}

		@Override
		public void visit(final StringArrayAttributeType attributeType) {
		}
	}
}
