package org.cmdbuild.service.rest.v1.model;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.immutableEntry;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.utils.guava.Functions.toKey;
import static org.cmdbuild.common.utils.guava.Functions.toValue;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.service.rest.v1.model.Attribute.Filter;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithFullDetails.AttributeStatus;

import com.google.common.base.Function;

public class Models {

	private static abstract class ModelBuilder<T extends Model> implements org.apache.commons.lang3.builder.Builder<T> {

		@Override
		public final T build() {
			doValidate();
			return doBuild();
		}

		protected void doValidate() {
			// no validation required
		}

		protected abstract T doBuild();

		@Override
		public final String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
		}

	}

	public static class AttachmentBuilder extends ModelBuilder<Attachment> {

		private static final Values NO_METADATA = newValues().build();

		private String id;
		private String name;
		private String category;
		private String description;
		private String version;
		private String author;
		private String created;
		private String modified;
		private Values metadata;

		private AttachmentBuilder() {
			// use factory method
		}

		private AttachmentBuilder(final Attachment existing) {
			// use factory method
			this.id = existing.getId();
			this.name = existing.getName();
			this.category = existing.getCategory();
			this.description = existing.getDescription();
			this.version = existing.getVersion();
			this.author = existing.getAuthor();
			this.created = existing.getCreated();
			this.modified = existing.getModified();
			this.metadata = defaultIfNull(existing.getMetadata(), NO_METADATA);
		}

		@Override
		protected Attachment doBuild() {
			final Attachment output = new Attachment();
			output.setId(id);
			output.setName(name);
			output.setCategory(category);
			output.setDescription(description);
			output.setVersion(version);
			output.setAuthor(author);
			output.setCreated(created);
			output.setModified(modified);
			output.setMetadata(metadata);
			return output;
		}

		public AttachmentBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public AttachmentBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public AttachmentBuilder withCategory(final String category) {
			this.category = category;
			return this;
		}

		public AttachmentBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public AttachmentBuilder withVersion(final String version) {
			this.version = version;
			return this;
		}

		public AttachmentBuilder withAuthor(final String author) {
			this.author = author;
			return this;
		}

		public AttachmentBuilder withCreated(final String created) {
			this.created = created;
			return this;
		}

		public AttachmentBuilder withModified(final String modified) {
			this.modified = modified;
			return this;
		}

		public AttachmentBuilder withMetadata(final Values metadata) {
			this.metadata = metadata;
			return this;
		}

	}

	public static class AttachmentCategoryBuilder extends ModelBuilder<AttachmentCategory> {

		private String id;
		private String description;

		private AttachmentCategoryBuilder() {
			// use factory method
		}

		@Override
		protected AttachmentCategory doBuild() {
			final AttachmentCategory output = new AttachmentCategory();
			output.setId(id);
			output.setDescription(description);
			return output;
		}

		public AttachmentCategoryBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public AttachmentCategoryBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class AttributeBuilder extends ModelBuilder<Attribute> {

		private static final Iterable<String> NO_VALUES = emptyList();

		private String id;
		private String type;
		private String name;
		private String description;
		private Boolean displayableInList;
		private String domainName;
		private Boolean unique;
		private Boolean mandatory;
		private Boolean inherited;
		private Boolean active;
		private Long index;
		private String defaultValue;
		private String group;
		private Long precision;
		private Long scale;
		private String targetClass;
		private Long length;
		private String editorType;
		private String lookupType;
		private Attribute.Filter filter;
		private Iterable<String> values;
		private Boolean writable;
		private Boolean hidden;

		private AttributeBuilder() {
			// use factory method
		}

		@Override
		protected Attribute doBuild() {
			final Attribute output = new Attribute();
			output.setId(id);
			output.setType(type);
			output.setName(name);
			output.setDescription(description);
			output.setDisplayableInList(isTrue(displayableInList));
			output.setDomainName(domainName);
			output.setUnique(isTrue(unique));
			output.setMandatory(isTrue(mandatory));
			output.setInherited(isTrue(inherited));
			output.setActive(isTrue(active));
			output.setIndex(index);
			output.setDefaultValue(defaultValue);
			output.setGroup(group);
			output.setPrecision(precision);
			output.setScale(scale);
			output.setTargetClass(targetClass);
			output.setLength(length);
			output.setEditorType(editorType);
			output.setLookupType(lookupType);
			output.setFilter(filter);
			output.setValues(newArrayList(defaultIfNull(values, NO_VALUES)));
			output.setWritable(isTrue(writable));
			output.setHidden(isTrue(hidden));
			return output;
		}

		public AttributeBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public AttributeBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public AttributeBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public AttributeBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public AttributeBuilder thatIsDisplayableInList(final Boolean displayableInList) {
			this.displayableInList = displayableInList;
			return this;
		}

		public AttributeBuilder withDomainName(final String domainName) {
			this.domainName = domainName;
			return this;
		}

		public AttributeBuilder thatIsUnique(final Boolean unique) {
			this.unique = unique;
			return this;
		}

		public AttributeBuilder thatIsMandatory(final Boolean mandatory) {
			this.mandatory = mandatory;
			return this;
		}

		public AttributeBuilder thatIsInherited(final Boolean inherited) {
			this.inherited = inherited;
			return this;
		}

		public AttributeBuilder thatIsActive(final Boolean active) {
			this.active = active;
			return this;
		}

		public AttributeBuilder withIndex(final Long index) {
			this.index = index;
			return this;
		}

		public AttributeBuilder withDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public AttributeBuilder withGroup(final String group) {
			this.group = group;
			return this;
		}

		public AttributeBuilder withPrecision(final Long precision) {
			this.precision = precision;
			return this;
		}

		public AttributeBuilder withScale(final Long scale) {
			this.scale = scale;
			return this;
		}

		public AttributeBuilder withTargetClass(final String targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public AttributeBuilder withLength(final Long length) {
			this.length = length;
			return this;
		}

		public AttributeBuilder withEditorType(final String editorType) {
			this.editorType = editorType;
			return this;
		}

		public AttributeBuilder withLookupType(final String lookupType) {
			this.lookupType = lookupType;
			return this;
		}

		public AttributeBuilder withFilter(final Attribute.Filter filter) {
			this.filter = filter;
			return this;
		}

		public AttributeBuilder withValues(final Iterable<String> values) {
			this.values = values;
			return this;
		}

		public AttributeBuilder thatIsWritable(final Boolean writable) {
			this.writable = writable;
			return this;
		}

		public AttributeBuilder thatIsHidden(final Boolean hidden) {
			this.hidden = hidden;
			return this;
		}

	}

	public static class AttributeStatusBuilder extends ModelBuilder<AttributeStatus> {

		private String id;
		private Boolean writable;
		private Boolean mandatory;
		private Long index;

		private AttributeStatusBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			writable = defaultIfNull(writable, FALSE);
			mandatory = defaultIfNull(mandatory, FALSE);
		}

		@Override
		protected AttributeStatus doBuild() {
			final AttributeStatus output = new AttributeStatus();
			output.setId(id);
			output.setWritable(writable);
			output.setMandatory(mandatory);
			output.setIndex(index);
			return output;
		}

		public AttributeStatusBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public AttributeStatusBuilder withWritable(final Boolean writable) {
			this.writable = writable;
			return this;
		}

		public AttributeStatusBuilder withMandatory(final Boolean mandatory) {
			this.mandatory = mandatory;
			return this;
		}

		public AttributeStatusBuilder withIndex(final Long index) {
			this.index = index;
			return this;
		}

	}

	public static class CardBuilder extends ModelBuilder<Card> {

		private static final Values NO_VALUES = newValues().build();

		private String type;
		private Long id;
		private final Values values = newValues().build();

		private CardBuilder() {
			// use factory method
		}

		@Override
		protected Card doBuild() {
			final Card output = new Card();
			output.setType(type);
			output.setId(id);
			output.setValues(defaultIfNull(values, NO_VALUES));
			return output;
		}

		public CardBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public CardBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public CardBuilder withValue(final String name, final Object value) {
			return withValue(immutableEntry(name, value));
		}

		public CardBuilder withValue(final Entry<String, ? extends Object> value) {
			return withValues(asList(value));
		}

		public CardBuilder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			final Function<Entry<? extends String, ? extends Object>, String> key = toKey();
			final Function<Entry<? extends String, ? extends Object>, Object> value = toValue();
			final Map<String, Object> allValues = transformValues(uniqueIndex(values, key), value);
			return withValues(allValues);
		}

		public CardBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static class ClassPrivilegeBuilder extends ModelBuilder<ClassPrivilege> {

		private String id;
		private String name;
		private String description;
		private String mode;

		private ClassPrivilegeBuilder() {
			// use factory method
		}

		@Override
		protected ClassPrivilege doBuild() {
			final ClassPrivilege output = new ClassPrivilege();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setMode(mode);
			return output;
		}

		public ClassPrivilegeBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ClassPrivilegeBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ClassPrivilegeBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ClassPrivilegeBuilder withMode(final String mode) {
			this.mode = mode;
			return this;
		}

	}

	public static class ClassWithBasicDetailsBuilder extends ModelBuilder<ClassWithBasicDetails> {

		private String id;
		private String name;
		private String description;
		private String parent;
		private Boolean prototype;

		private ClassWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		@Override
		protected ClassWithBasicDetails doBuild() {
			final ClassWithBasicDetails output = new ClassWithBasicDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setParent(parent);
			output.setPrototype(prototype);
			return output;
		}

		public ClassWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ClassWithBasicDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ClassWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ClassWithBasicDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

		public ClassWithBasicDetailsBuilder thatIsPrototype(final Boolean prototype) {
			this.prototype = prototype;
			return this;
		}

	}

	public static class ClassWithFullDetailsBuilder extends ModelBuilder<ClassWithFullDetails> {

		private String id;
		private String name;
		private String description;
		private Boolean prototype;
		private String descriptionAttributeName;
		private String parent;

		private ClassWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		@Override
		protected ClassWithFullDetails doBuild() {
			final ClassWithFullDetails output = new ClassWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setPrototype(prototype);
			output.setDescriptionAttributeName(descriptionAttributeName);
			output.setParent(parent);
			return output;

		}

		public ClassWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ClassWithFullDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ClassWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ClassWithFullDetailsBuilder thatIsPrototype(final Boolean superclass) {
			this.prototype = superclass;
			return this;
		}

		public ClassWithFullDetailsBuilder withDescriptionAttributeName(final String descriptionAttributeName) {
			this.descriptionAttributeName = descriptionAttributeName;
			return this;
		}

		public ClassWithFullDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class DomainWithBasicDetailsBuilder extends ModelBuilder<DomainWithBasicDetails> {

		private String id;
		private String name;
		private String description;

		private DomainWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected DomainWithBasicDetails doBuild() {
			final DomainWithBasicDetails output = new DomainWithBasicDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			return output;
		}

		public DomainWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public DomainWithBasicDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public DomainWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class DomainWithFullDetailsBuilder extends ModelBuilder<DomainWithFullDetails> {

		private String id;
		private String name;
		private String description;
		private String source;
		private boolean sourceProcess;
		private String destination;
		private boolean destinationProcess;
		private String cardinality;
		private String descriptionDirect;
		private String descriptionInverse;
		private String descriptionMasterDetail;

		private DomainWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected DomainWithFullDetails doBuild() {
			final DomainWithFullDetails output = new DomainWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setSource(source);
			output.setSourceProcess(sourceProcess);
			output.setDestination(destination);
			output.setDestinationProcess(destinationProcess);
			output.setCardinality(cardinality);
			output.setDescriptionDirect(descriptionDirect);
			output.setDescriptionInverse(descriptionInverse);
			output.setDescriptionMasterDetail(descriptionMasterDetail);
			return output;
		}

		public DomainWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public DomainWithFullDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public DomainWithFullDetailsBuilder withSource(final String source) {
			this.source = source;
			return this;
		}

		public DomainWithFullDetailsBuilder withSourceProcess(final boolean sourceProcess) {
			this.sourceProcess = sourceProcess;
			return this;
		}

		public DomainWithFullDetailsBuilder withDestination(final String destination) {
			this.destination = destination;
			return this;
		}

		public DomainWithFullDetailsBuilder withDestinationProcess(final boolean destinationProcess) {
			this.destinationProcess = destinationProcess;
			return this;
		}

		public DomainWithFullDetailsBuilder withCardinality(final String cardinality) {
			this.cardinality = cardinality;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescriptionDirect(final String descriptionDirect) {
			this.descriptionDirect = descriptionDirect;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescriptionInverse(final String descriptionInverse) {
			this.descriptionInverse = descriptionInverse;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescriptionMasterDetail(final String descriptionMasterDetail) {
			this.descriptionMasterDetail = descriptionMasterDetail;
			return this;
		}

	}

	public static class FilterBuilder extends ModelBuilder<Filter> {

		private String text;
		private Map<String, String> params;

		private FilterBuilder() {
			// use factory method
		}

		@Override
		protected Filter doBuild() {
			final Filter output = new Filter();
			output.setText(text);
			output.setParams(params);
			return output;
		}

		public FilterBuilder withText(final String text) {
			this.text = text;
			return this;
		}

		public FilterBuilder withParams(final Map<String, String> params) {
			this.params = params;
			return this;
		}

	}

	public static class LookupDetailBuilder extends ModelBuilder<LookupDetail> {

		private Long id;
		private String code;
		private String description;
		private String type;
		private Long number;
		private Boolean active;
		private Boolean isDefault;
		private Long parentId;
		private String parentType;

		private LookupDetailBuilder() {
			// use factory method
		}

		@Override
		protected LookupDetail doBuild() {
			final LookupDetail output = new LookupDetail();
			output.setId(id);
			output.setCode(code);
			output.setDescription(description);
			output.setType(type);
			output.setNumber(number);
			output.setActive(active);
			output.setDefault(isDefault);
			output.setParentId(parentId);
			output.setParentType(parentType);
			return output;
		}

		public LookupDetailBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public LookupDetailBuilder withCode(final String code) {
			this.code = code;
			return this;
		}

		public LookupDetailBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public LookupDetailBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public LookupDetailBuilder withNumber(final Long number) {
			this.number = number;
			return this;
		}

		public LookupDetailBuilder thatIsActive(final Boolean active) {
			this.active = active;
			return this;
		}

		public LookupDetailBuilder thatIsDefault(final Boolean isDefault) {
			this.isDefault = isDefault;
			return this;
		}

		public LookupDetailBuilder withParentId(final Long parentId) {
			this.parentId = parentId;
			return this;
		}

		public LookupDetailBuilder withParentType(final String parentType) {
			this.parentType = parentType;
			return this;
		}

	}

	public static class LookupTypeDetailBuilder extends ModelBuilder<LookupTypeDetail> {

		private String id;
		private String name;
		private String parent;

		private LookupTypeDetailBuilder() {
			// use factory method
		}

		@Override
		protected LookupTypeDetail doBuild() {
			final LookupTypeDetail output = new LookupTypeDetail();
			output.setId(id);
			output.setName(name);
			output.setParent(parent);
			return output;
		}

		public LookupTypeDetailBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public LookupTypeDetailBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public LookupTypeDetailBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class MenuBuilder extends ModelBuilder<MenuDetail> {

		private static final Iterable<MenuDetail> NO_CHILDREN = emptyList();

		private String menuType;
		private Long index;
		private String objectType;
		private Long objectId;
		private String objectDescription;
		private Iterable<MenuDetail> children;

		private MenuBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			index = defaultIfNull(index, 0L);
			children = defaultIfNull(children, NO_CHILDREN);
		}

		@Override
		protected MenuDetail doBuild() {
			final MenuDetail output = new MenuDetail();
			output.setMenuType(menuType);
			output.setIndex(index);
			output.setObjectType(objectType);
			output.setObjectId(objectId);
			output.setObjectDescription(objectDescription);
			output.setChildren(newArrayList(children));
			return output;
		}

		public MenuBuilder withMenuType(final String menuType) {
			this.menuType = menuType;
			return this;
		}

		public MenuBuilder withIndex(final Long index) {
			this.index = index;
			return this;
		}

		public MenuBuilder withObjectType(final String objectType) {
			this.objectType = objectType;
			return this;
		}

		public MenuBuilder withObjectId(final Long objectId) {
			this.objectId = objectId;
			return this;
		}

		public MenuBuilder withObjectDescription(final String objectDescription) {
			this.objectDescription = objectDescription;
			return this;
		}

		public MenuBuilder withChildren(final Iterable<MenuDetail> children) {
			this.children = children;
			return this;
		}

	}

	public static class MetadataBuilder extends ModelBuilder<DetailResponseMetadata> {

		private static final Map<Long, Long> NO_POSITIONS = emptyMap();

		private Long total;
		private Map<Long, Long> positions;

		private MetadataBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			super.doValidate();
			positions = defaultIfNull(positions, NO_POSITIONS);
		}

		@Override
		protected DetailResponseMetadata doBuild() {
			final DetailResponseMetadata output = new DetailResponseMetadata();
			output.setTotal(total);
			output.setPositions(positions);
			return output;
		}

		public MetadataBuilder withTotal(final Long total) {
			this.total = total;
			return this;
		}

		public MetadataBuilder withPositions(final Map<Long, Long> positions) {
			this.positions = positions;
			return this;
		}

	}

	public static class ProcessActivityWithBasicDetailsBuilder extends ModelBuilder<ProcessActivityWithBasicDetails> {

		private String id;
		private Boolean writable;
		private String description;

		private ProcessActivityWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			writable = defaultIfNull(writable, FALSE);
		}

		@Override
		protected ProcessActivityWithBasicDetails doBuild() {
			final ProcessActivityWithBasicDetails output = new ProcessActivityWithBasicDetails();
			output.setId(id);
			output.setWritable(writable);
			output.setDescription(description);
			return output;
		}

		public ProcessActivityWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessActivityWithBasicDetailsBuilder withWritableStatus(final boolean writable) {
			this.writable = writable;
			return this;
		}

		public ProcessActivityWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class ProcessActivityWithFullDetailsBuilder extends ModelBuilder<ProcessActivityWithFullDetails> {

		private static final Collection<? extends AttributeStatus> NO_ATTRIBUTES = emptyList();
		private static final Collection<? extends Widget> NO_WIDGETS = emptyList();

		private String id;
		private boolean writable;
		private String description;
		private String instructions;
		private final Collection<AttributeStatus> attributes = newArrayList();
		private final Collection<Widget> widgets = newArrayList();

		private ProcessActivityWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected ProcessActivityWithFullDetails doBuild() {
			final ProcessActivityWithFullDetails output = new ProcessActivityWithFullDetails();
			output.setId(id);
			output.setWritable(writable);
			output.setDescription(description);
			output.setInstructions(instructions);
			output.setAttributes(attributes);
			output.setWidgets(widgets);
			return output;
		}

		public ProcessActivityWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withWritableStatus(final boolean writable) {
			this.writable = writable;
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withInstructions(final String instructions) {
			this.instructions = instructions;
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withAttribute(
				final ProcessActivityWithFullDetails.AttributeStatus attribute) {
			return withAttributes(asList(attribute));
		}

		public ProcessActivityWithFullDetailsBuilder withAttributes(
				final Iterable<? extends ProcessActivityWithFullDetails.AttributeStatus> attributes) {
			addAll(this.attributes, defaultIfNull(attributes, NO_ATTRIBUTES));
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withWidgets(final Iterable<? extends Widget> widgets) {
			addAll(this.widgets, defaultIfNull(widgets, NO_WIDGETS));
			return this;
		}

	}

	public static class ProcessInstanceBuilder extends ModelBuilder<ProcessInstance> {

		private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
		private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

		private String type;
		private Long id;
		private String name;
		private Long status;
		private final Values values = newValues().build();

		private ProcessInstanceBuilder() {
			// use factory method
		}

		@Override
		protected ProcessInstance doBuild() {
			final ProcessInstance output = new ProcessInstance();
			output.setType(type);
			output.setId(id);
			output.setName(name);
			output.setStatus(status);
			output.setValues(values);
			return output;
		}

		public ProcessInstanceBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public ProcessInstanceBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public ProcessInstanceBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessInstanceBuilder withStatus(final Long status) {
			this.status = status;
			return this;
		}

		public ProcessInstanceBuilder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			return withValues(transformValues(uniqueIndex(values, KEY), VALUE));
		}

		public ProcessInstanceBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static class ProcessInstanceAdvanceBuilder extends ModelBuilder<ProcessInstanceAdvanceable> {

		private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
		private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

		private String type;
		private Long id;
		private String name;
		private Long status;
		final Values values = newValues().build();
		private String activityId;
		private Boolean advance;

		private ProcessInstanceAdvanceBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			advance = defaultIfNull(advance, FALSE);
		}

		@Override
		protected ProcessInstanceAdvanceable doBuild() {
			final ProcessInstanceAdvanceable output = new ProcessInstanceAdvanceable();
			output.setType(type);
			output.setId(id);
			output.setName(name);
			output.setStatus(status);
			output.setValues(values);
			output.setActivity(activityId);
			output.setAdvance(advance);
			return output;
		}

		public ProcessInstanceAdvanceBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withStatus(final Long status) {
			this.status = status;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withValues(
				final Iterable<? extends Entry<String, ? extends Object>> values) {
			return withValues(transformValues(uniqueIndex(values, KEY), VALUE));
		}

		public ProcessInstanceAdvanceBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

		public ProcessInstanceAdvanceBuilder withActivity(final String activityId) {
			this.activityId = activityId;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withAdvance(final boolean advance) {
			this.advance = advance;
			return this;
		}

	}

	public static class ProcessStatusBuilder extends ModelBuilder<ProcessStatus> {

		private Long id;
		private String value;
		private String description;

		private ProcessStatusBuilder() {
			// use factory method
		}

		@Override
		protected ProcessStatus doBuild() {
			final ProcessStatus output = new ProcessStatus();
			output.setId(id);
			output.setValue(value);
			output.setDescription(description);
			return output;
		}

		public ProcessStatusBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public ProcessStatusBuilder withValue(final String value) {
			this.value = value;
			return this;
		}

		public ProcessStatusBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class ProcessWithBasicDetailsBuilder extends ModelBuilder<ProcessWithBasicDetails> {

		private String id;
		private String name;
		private String description;
		private String parent;
		private Boolean prototype;

		private ProcessWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		@Override
		protected ProcessWithBasicDetails doBuild() {
			final ProcessWithBasicDetails output = new ProcessWithBasicDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setParent(parent);
			output.setPrototype(prototype);
			return output;
		}

		public ProcessWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessWithBasicDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ProcessWithBasicDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

		public ProcessWithBasicDetailsBuilder thatIsPrototype(final Boolean prototype) {
			this.prototype = prototype;
			return this;
		}

	}

	public static class ProcessWithFullDetailsBuilder extends ModelBuilder<ProcessWithFullDetails> {

		private static final Collection<Long> NO_STATUSES = emptyList();

		private String id;
		private String name;
		private String description;
		private Boolean prototype;
		private String descriptionAttributeName;
		private Collection<Long> statuses;
		private Long defaultStatus;
		private String parent;

		private ProcessWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
			statuses = defaultIfNull(statuses, NO_STATUSES);
		}

		@Override
		protected ProcessWithFullDetails doBuild() {
			final ProcessWithFullDetails output = new ProcessWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setPrototype(prototype);
			output.setDescriptionAttributeName(descriptionAttributeName);
			output.setStatuses(statuses);
			output.setDefaultStatus(defaultStatus);
			output.setParent(parent);
			return output;
		}

		public ProcessWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessWithFullDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ProcessWithFullDetailsBuilder thatIsPrototype(final Boolean superclass) {
			this.prototype = superclass;
			return this;
		}

		public ProcessWithFullDetailsBuilder withDescriptionAttributeName(final String descriptionAttributeName) {
			this.descriptionAttributeName = descriptionAttributeName;
			return this;
		}

		public ProcessWithFullDetailsBuilder withStatuses(final Iterable<? extends Long> statuses) {
			this.statuses = newArrayList(defaultIfNull(statuses, NO_STATUSES));
			return this;
		}

		public ProcessWithFullDetailsBuilder withDefaultStatus(final Long defaultStatus) {
			this.defaultStatus = defaultStatus;
			return this;
		}

		public ProcessWithFullDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class RelationBuilder extends ModelBuilder<Relation> {

		private String type;
		private Long id;
		private Card source;
		private Card destination;
		private final Values values = newValues().build();

		private RelationBuilder() {
			// use factory method
		}

		@Override
		protected Relation doBuild() {
			final Relation output = new Relation();
			output.setType(type);
			output.setId(id);
			output.setSource(source);
			output.setDestination(destination);
			output.setValues(values);
			return output;
		}

		public RelationBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public RelationBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public RelationBuilder withSource(final Card source) {
			this.source = source;
			return this;
		}

		public RelationBuilder withDestination(final Card destination) {
			this.destination = destination;
			return this;
		}

		public RelationBuilder withValue(final String name, final Object value) {
			return withValue(immutableEntry(name, value));
		}

		public RelationBuilder withValue(final Entry<String, ? extends Object> value) {
			return withValues(asList(value));
		}

		public RelationBuilder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			final Function<Entry<? extends String, ? extends Object>, String> key = toKey();
			final Function<Entry<? extends String, ? extends Object>, Object> value = toValue();
			final Map<String, Object> allValues = transformValues(uniqueIndex(values, key), value);
			return withValues(allValues);
		}

		public RelationBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static class ResponseSingleBuilder<T> extends ModelBuilder<ResponseSingle<T>> {

		private T element;

		private ResponseSingleBuilder() {
			// use factory method
		}

		@Override
		protected ResponseSingle<T> doBuild() {
			final ResponseSingle<T> output = new ResponseSingle<T>();
			output.setElement(element);
			return output;
		}

		public ResponseSingleBuilder<T> withElement(final T element) {
			this.element = element;
			return this;
		}

	}

	public static class ResponseMultipleBuilder<T> extends ModelBuilder<ResponseMultiple<T>> {

		private final Iterable<T> NO_ELEMENTS = emptyList();

		private final Collection<T> elements = newArrayList();
		private DetailResponseMetadata metadata;

		private ResponseMultipleBuilder() {
			// use factory method
		}

		@Override
		protected ResponseMultiple<T> doBuild() {
			final ResponseMultiple<T> output = new ResponseMultiple<T>();
			output.setElements(elements);
			output.setMetadata(metadata);
			return output;
		}

		@SuppressWarnings("unchecked")
		public ResponseMultipleBuilder<T> withElement(final T element) {
			addAll(this.elements, (element == null) ? NO_ELEMENTS : asList(element));
			return this;
		}

		public ResponseMultipleBuilder<T> withElements(final Iterable<T> elements) {
			addAll(this.elements, defaultIfNull(elements, NO_ELEMENTS));
			return this;
		}

		public ResponseMultipleBuilder<T> withMetadata(final DetailResponseMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

	}

	public static class SessionBuilder extends ModelBuilder<Session> {

		private static final Collection<String> NO_ROLES = emptyList();

		private String id;
		private String username;
		private String password;
		private String role;
		private final Collection<String> availableRoles = newHashSet();

		private SessionBuilder() {
			// use factory method
		}

		private SessionBuilder(final Session existing) {
			// use factory method
			this.id = existing.getId();
			this.username = existing.getUsername();
			this.password = existing.getPassword();
			this.role = existing.getRole();
			this.availableRoles.addAll(defaultIfNull(existing.getAvailableRoles(), NO_ROLES));
		}

		@Override
		protected Session doBuild() {
			final Session output = new Session();
			output.setId(id);
			output.setUsername(username);
			output.setPassword(password);
			output.setRole(role);
			output.setAvailableRoles(availableRoles);
			return output;
		}

		public SessionBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public SessionBuilder withUsername(final String username) {
			this.username = username;
			return this;
		}

		public SessionBuilder withPassword(final String password) {
			this.password = password;
			return this;
		}

		public SessionBuilder withRole(final String role) {
			this.role = role;
			return this;
		}

		public SessionBuilder withAvailableRoles(final Iterable<String> availableRoles) {
			this.availableRoles.clear();
			addAll(this.availableRoles, defaultIfNull(availableRoles, NO_ROLES));
			return this;
		}

	}

	public static class ValuesBuilder extends ModelBuilder<Values> {

		private static final Map<String, ? extends Object> NO_VALUES = emptyMap();

		private Map<String, ? extends Object> values;

		private ValuesBuilder() {
			// use factory method
		}

		@Override
		protected Values doBuild() {
			final Values output = new Values();
			output.putAll(defaultIfNull(values, NO_VALUES));
			return output;
		}

		public ValuesBuilder withValues(final Map<String, ? extends Object> values) {
			this.values = values;
			return this;
		}

	}

	public static class WidgetBuilder extends ModelBuilder<Widget> {

		private String id;
		private String type;
		private boolean active;
		private boolean required;
		private String label;
		private Values data;

		private WidgetBuilder() {
			// use factory method
		}

		@Override
		protected Widget doBuild() {
			final Widget output = new Widget();
			output.setId(id);
			output.setType(type);
			output.setActive(active);
			output.setRequired(required);
			output.setLabel(label);
			output.setData(data);
			return output;
		}

		public WidgetBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public WidgetBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public WidgetBuilder withActive(final boolean active) {
			this.active = active;
			return this;
		}

		public WidgetBuilder withRequired(final boolean required) {
			this.required = required;
			return this;
		}

		public WidgetBuilder withLabel(final String label) {
			this.label = label;
			return this;
		}

		public WidgetBuilder withData(final Values data) {
			this.data = data;
			return this;
		}

	}

	public static AttachmentBuilder newAttachment() {
		return new AttachmentBuilder();
	}

	public static AttachmentBuilder newAttachment(final Attachment attachment) {
		return new AttachmentBuilder(attachment);
	}

	private static final Attachment NULL_ATTACHMENT = newAttachment().build();

	public static Attachment nullAttachment() {
		return NULL_ATTACHMENT;
	}

	public static AttachmentCategoryBuilder newAttachmentCategory() {
		return new AttachmentCategoryBuilder();
	}

	public static AttributeBuilder newAttribute() {
		return new AttributeBuilder();
	}

	public static AttributeStatusBuilder newAttributeStatus() {
		return new AttributeStatusBuilder();
	}

	public static CardBuilder newCard() {
		return new CardBuilder();
	}

	public static ClassPrivilegeBuilder newClassPrivilege() {
		return new ClassPrivilegeBuilder();
	}

	public static ClassWithBasicDetailsBuilder newClassWithBasicDetails() {
		return new ClassWithBasicDetailsBuilder();
	}

	public static ClassWithFullDetailsBuilder newClassWithFullDetails() {
		return new ClassWithFullDetailsBuilder();
	}

	public static DomainWithBasicDetailsBuilder newDomainWithBasicDetails() {
		return new DomainWithBasicDetailsBuilder();
	}

	public static DomainWithFullDetailsBuilder newDomainWithFullDetails() {
		return new DomainWithFullDetailsBuilder();
	}

	public static FilterBuilder newFilter() {
		return new FilterBuilder();
	}

	public static LookupDetailBuilder newLookupDetail() {
		return new LookupDetailBuilder();
	}

	public static LookupTypeDetailBuilder newLookupTypeDetail() {
		return new LookupTypeDetailBuilder();
	}

	public static MenuBuilder newMenu() {
		return new MenuBuilder();
	}

	public static MetadataBuilder newMetadata() {
		return new MetadataBuilder();
	}

	public static ProcessActivityWithBasicDetailsBuilder newProcessActivityWithBasicDetails() {
		return new ProcessActivityWithBasicDetailsBuilder();
	}

	public static ProcessActivityWithFullDetailsBuilder newProcessActivityWithFullDetails() {
		return new ProcessActivityWithFullDetailsBuilder();
	}

	public static ProcessInstanceBuilder newProcessInstance() {
		return new ProcessInstanceBuilder();
	}

	public static ProcessInstanceAdvanceBuilder newProcessInstanceAdvance() {
		return new ProcessInstanceAdvanceBuilder();
	}

	public static ProcessStatusBuilder newProcessStatus() {
		return new ProcessStatusBuilder();
	}

	public static ProcessWithBasicDetailsBuilder newProcessWithBasicDetails() {
		return new ProcessWithBasicDetailsBuilder();
	}

	public static ProcessWithFullDetailsBuilder newProcessWithFullDetails() {
		return new ProcessWithFullDetailsBuilder();
	}

	public static RelationBuilder newRelation() {
		return new RelationBuilder();
	}

	@Deprecated
	public static <T> ResponseSingleBuilder<T> newResponseSingle() {
		return new ResponseSingleBuilder<T>();
	}

	public static <T> ResponseSingleBuilder<T> newResponseSingle(final Class<T> type) {
		return newResponseSingle();
	}

	@Deprecated
	public static <T> ResponseMultipleBuilder<T> newResponseMultiple() {
		return new ResponseMultipleBuilder<T>();
	}

	public static <T> ResponseMultipleBuilder<T> newResponseMultiple(final Class<T> type) {
		return newResponseMultiple();
	}

	public static SessionBuilder newSession() {
		return new SessionBuilder();
	}

	public static ValuesBuilder newValues() {
		return new ValuesBuilder();
	}

	public static WidgetBuilder newWidget() {
		return new WidgetBuilder();
	}

	private Models() {
		// prevents instantation
	}

}
