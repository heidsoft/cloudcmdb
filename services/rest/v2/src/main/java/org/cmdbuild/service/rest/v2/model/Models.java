package org.cmdbuild.service.rest.v2.model;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.immutableEntry;
import static com.google.common.collect.Maps.newHashMap;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.service.rest.v2.model.Attribute.Filter;
import org.cmdbuild.service.rest.v2.model.ClassWithFullDetails.AttributeOrder;
import org.cmdbuild.service.rest.v2.model.DetailResponseMetadata.Reference;
import org.cmdbuild.service.rest.v2.model.ProcessActivityWithFullDetails.AttributeStatus;

import com.google.common.base.Function;

public class Models {

	private static final Collection<AttributeOrder> NO_ORDER = emptyList();

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
		private static final Map<String, String> NO_METADATA = emptyMap();

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
		private String targetType;
		private Long length;
		private String editorType;
		private String lookupType;
		private Attribute.Filter filter;
		private Iterable<String> values;
		private Boolean writable;
		private Boolean hidden;
		private Map<String, String> metadata;

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
			output.setTargetType(targetType);
			output.setLength(length);
			output.setEditorType(editorType);
			output.setLookupType(lookupType);
			output.setFilter(filter);
			output.setValues(newArrayList(defaultIfNull(values, NO_VALUES)));
			output.setWritable(isTrue(writable));
			output.setHidden(isTrue(hidden));
			output.setMetadata(new HashMap<>(defaultIfNull(metadata, NO_METADATA)));
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

		public AttributeBuilder withTargetType(final String targetType) {
			this.targetType = targetType;
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

		public AttributeBuilder withMetadata(final Map<String, String> metadata) {
			this.metadata = metadata;
			return this;
		}

	}

	public static class AttributeOrderBuilder extends ModelBuilder<AttributeOrder> {

		private String attribute;
		private String direction;

		private AttributeOrderBuilder() {
			// use factory method
		}

		@Override
		protected AttributeOrder doBuild() {
			final AttributeOrder output = new AttributeOrder();
			output.setAttribute(attribute);
			output.setDirection(direction);
			return output;
		}

		public AttributeOrderBuilder withAttribute(final String attribute) {
			this.attribute = attribute;
			return this;
		}

		public AttributeOrderBuilder withDirection(final String direction) {
			this.direction = direction;
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
		private Collection<AttributeOrder> defaultOrder;
		private String parent;

		private ClassWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
			defaultOrder = defaultIfNull(defaultOrder, NO_ORDER);
		}

		@Override
		protected ClassWithFullDetails doBuild() {
			final ClassWithFullDetails output = new ClassWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setPrototype(prototype);
			output.setDescriptionAttributeName(descriptionAttributeName);
			output.setDefaultOrder(defaultOrder);
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

		public ClassWithFullDetailsBuilder withDefaultOrder(final Collection<AttributeOrder> defaultOrder) {
			this.defaultOrder = defaultOrder;
			return this;
		}

		public ClassWithFullDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class DomainTreeBuilder extends ModelBuilder<DomainTree> {

		private String id;
		private String description;
		private final Collection<Node> nodes = newArrayList();

		private DomainTreeBuilder() {
			// use factory method
		}

		@Override
		protected DomainTree doBuild() {
			final DomainTree output = new DomainTree();
			output.setId(id);
			output.setDescription(description);
			output.setNodes(nodes);
			return output;
		}

		public DomainTreeBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public DomainTreeBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public DomainTreeBuilder withNode(final Node node) {
			this.nodes.add(node);
			return this;
		}

		public DomainTreeBuilder withNodes(final Iterable<Node> nodes) {
			addAll(this.nodes, nodes);
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
		private boolean active;

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
			output.setActive(active);
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

		public DomainWithFullDetailsBuilder withActive(final boolean active) {
			this.active = active;
			return this;
		}

	}

	public static class EmailBuilder extends ModelBuilder<Email> {

		private Long id;
		private String from;
		private String to;
		private String cc;
		private String bcc;
		private String subject;
		private String body;
		private String notifyWith;
		private String date;
		private String status;
		private boolean noSubjectPrefix;
		private String account;
		private String template;
		private boolean keepSynchronization;
		private boolean promptSynchronization;
		private long delay;

		private EmailBuilder() {
			// use factory method
		}

		@Override
		protected Email doBuild() {
			final Email output = new Email();
			output.setId(id);
			output.setFrom(from);
			output.setTo(to);
			output.setCc(cc);
			output.setBcc(bcc);
			output.setSubject(subject);
			output.setBody(body);
			output.setDate(date);
			output.setStatus(status);
			output.setNotifyWith(notifyWith);
			output.setNoSubjectPrefix(noSubjectPrefix);
			output.setAccount(account);
			output.setTemplate(template);
			output.setKeepSynchronization(keepSynchronization);
			output.setPromptSynchronization(promptSynchronization);
			output.setDelay(delay);
			return output;
		}

		public EmailBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public EmailBuilder withFrom(final String from) {
			this.from = from;
			return this;
		}

		public EmailBuilder withTo(final String to) {
			this.to = to;
			return this;
		}

		public EmailBuilder withCc(final String cc) {
			this.cc = cc;
			return this;
		}

		public EmailBuilder withBcc(final String bcc) {
			this.bcc = bcc;
			return this;
		}

		public EmailBuilder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

		public EmailBuilder withBody(final String body) {
			this.body = body;
			return this;
		}

		public EmailBuilder withNotifyWith(final String notifyWith) {
			this.notifyWith = notifyWith;
			return this;
		}

		public EmailBuilder withDate(final String date) {
			this.date = date;
			return this;
		}

		public EmailBuilder withStatus(final String status) {
			this.status = status;
			return this;
		}

		public EmailBuilder withNoSubjectPrefix(final boolean noSubjectPrefix) {
			this.noSubjectPrefix = noSubjectPrefix;
			return this;
		}

		public EmailBuilder withAccount(final String account) {
			this.account = account;
			return this;
		}

		public EmailBuilder withTemplate(final String template) {
			this.template = template;
			return this;
		}

		public EmailBuilder withKeepSynchronization(final boolean keepSynchronization) {
			this.keepSynchronization = keepSynchronization;
			return this;
		}

		public EmailBuilder withPromptSynchronization(final boolean promptSynchronization) {
			this.promptSynchronization = promptSynchronization;
			return this;
		}

		public EmailBuilder withDelay(final long delay) {
			this.delay = delay;
			return this;
		}

	}

	public static class EmailTemplateBuilder extends ModelBuilder<EmailTemplate> {

		private String id;
		private String name;
		private String description;
		private String from;
		private String to;
		private String cc;
		private String bcc;
		private String subject;
		private String body;
		private String account;
		private boolean keepSynchronization;
		private boolean promptSynchronization;
		private long delay;

		private EmailTemplateBuilder() {
			// use factory method
		}

		@Override
		protected EmailTemplate doBuild() {
			final EmailTemplate output = new EmailTemplate();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setFrom(from);
			output.setTo(to);
			output.setCc(cc);
			output.setBcc(bcc);
			output.setSubject(subject);
			output.setBody(body);
			output.setAccount(account);
			output.setKeepSynchronization(keepSynchronization);
			output.setPromptSynchronization(promptSynchronization);
			output.setDelay(delay);
			return output;
		}

		public EmailTemplateBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public EmailTemplateBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public EmailTemplateBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public EmailTemplateBuilder withFrom(final String from) {
			this.from = from;
			return this;
		}

		public EmailTemplateBuilder withTo(final String to) {
			this.to = to;
			return this;
		}

		public EmailTemplateBuilder withCc(final String cc) {
			this.cc = cc;
			return this;
		}

		public EmailTemplateBuilder withBcc(final String bcc) {
			this.bcc = bcc;
			return this;
		}

		public EmailTemplateBuilder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

		public EmailTemplateBuilder withBody(final String body) {
			this.body = body;
			return this;
		}

		public EmailTemplateBuilder withAccount(final String account) {
			this.account = account;
			return this;
		}

		public EmailTemplateBuilder withKeepSynchronization(final boolean keepSynchronization) {
			this.keepSynchronization = keepSynchronization;
			return this;
		}

		public EmailTemplateBuilder withPromptSynchronization(final boolean promptSynchronization) {
			this.promptSynchronization = promptSynchronization;
			return this;
		}

		public EmailTemplateBuilder withDelay(final long delay) {
			this.delay = delay;
			return this;
		}

	}

	public static class FileSystemObjectBuilder extends ModelBuilder<FileSystemObject> {

		private String id;
		private String name;
		private String parent;

		private FileSystemObjectBuilder() {
			// use factory method
		}

		@Override
		protected FileSystemObject doBuild() {
			final FileSystemObject output = new FileSystemObject();
			output.setId(id);
			output.setName(name);
			output.setParent(parent);
			return output;
		}

		public FileSystemObjectBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public FileSystemObjectBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public FileSystemObjectBuilder withParent(final String parent) {
			this.parent = parent;
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

	public static class FunctionWithBasicDetailsBuilder extends ModelBuilder<FunctionWithBasicDetails> {

		private Long id;
		private String name;
		private String description;

		private FunctionWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected FunctionWithBasicDetails doBuild() {
			final FunctionWithBasicDetails output = new FunctionWithBasicDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			return output;
		}

		public FunctionWithBasicDetailsBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public FunctionWithBasicDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public FunctionWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class FunctionWithFullDetailsBuilder extends ModelBuilder<FunctionWithFullDetails> {

		private Long id;
		private String name;
		private String description;

		private FunctionWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected FunctionWithFullDetails doBuild() {
			final FunctionWithFullDetails output = new FunctionWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			return output;
		}

		public FunctionWithFullDetailsBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public FunctionWithFullDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public FunctionWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class GraphConfigurationBuilder extends ModelBuilder<GraphConfiguration> {

		private boolean enabled;
		private int baseLevel;
		private int clusteringThreshold;
		private String displayLabel;
		private String edgeColor;
		private boolean edgeTooltipEnabled;
		private boolean nodeTooltipEnabled;
		private int spriteDimension;
		private int stepRadius;
		private int viewPointDistance;
		private int viewPointHeight;

		@Override
		protected GraphConfiguration doBuild() {
			final GraphConfiguration output = new GraphConfiguration();
			output.setEnabled(enabled);
			output.setBaseLevel(baseLevel);
			output.setClusteringThreshold(clusteringThreshold);
			output.setDisplayLabel(displayLabel);
			output.setEdgeColor(edgeColor);
			output.setEdgeTooltipEnabled(edgeTooltipEnabled);
			output.setNodeTooltipEnabled(nodeTooltipEnabled);
			output.setSpriteDimension(spriteDimension);
			output.setStepRadius(stepRadius);
			output.setViewPointDistance(viewPointDistance);
			output.setViewPointHeight(viewPointHeight);
			return output;
		}

		public GraphConfigurationBuilder withEnabledStatus(final boolean value) {
			this.enabled = value;
			return this;
		}

		public GraphConfigurationBuilder withBaseLevel(final int value) {
			this.baseLevel = value;
			return this;
		}

		public GraphConfigurationBuilder withClusteringThreshold(final int value) {
			this.clusteringThreshold = value;
			return this;
		}

		public GraphConfigurationBuilder withDisplayLabel(final String value) {
			this.displayLabel = value;
			return this;
		}

		public GraphConfigurationBuilder withEdgeColor(final String value) {
			this.edgeColor = value;
			return this;
		}

		public GraphConfigurationBuilder withEdgeTooltipEnabled(final boolean value) {
			this.edgeTooltipEnabled = value;
			return this;
		}

		public GraphConfigurationBuilder withNodeTooltipEnabled(final boolean value) {
			this.nodeTooltipEnabled = value;
			return this;
		}

		public GraphConfigurationBuilder withSpriteDimension(final int value) {
			this.spriteDimension = value;
			return this;
		}

		public GraphConfigurationBuilder withStepRadius(final int value) {
			this.stepRadius = value;
			return this;
		}

		public GraphConfigurationBuilder withViewPointDistance(final int value) {
			this.viewPointDistance = value;
			return this;
		}

		public GraphConfigurationBuilder withViewPointHeight(final int value) {
			this.viewPointHeight = value;
			return this;
		}

	}

	public static class LongIdBuilder extends ModelBuilder<LongId> {

		private Long id;

		private LongIdBuilder() {
			// use factory method
		}

		@Override
		protected LongId doBuild() {
			final LongId output = new LongId();
			output.setId(id);
			return output;
		}

		public LongIdBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

	}

	public static class IconBuilder extends ModelBuilder<Icon> {

		private Long id;
		private String type;
		private final Map<String, Object> details = newHashMap();
		private Image image;

		private IconBuilder() {
			// use factory method
		}

		@Override
		protected Icon doBuild() {
			final Icon output = new Icon();
			output.setId(id);
			output.setType(type);
			output.setDetails(details);
			output.setImage(image);
			return output;
		}

		public IconBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public IconBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public IconBuilder withDetail(final String key, final Object value) {
			this.details.put(key, value);
			return this;
		}

		public IconBuilder withDetails(final Map<String, Object> details) {
			this.details.putAll(details);
			return this;
		}

		public IconBuilder withImage(final Image image) {
			this.image = image;
			return this;
		}

	}

	public static class ImageBuilder extends ModelBuilder<Image> {

		private String type;
		private final Map<String, Object> details = newHashMap();

		private ImageBuilder() {
			// use factory method
		}

		@Override
		protected Image doBuild() {
			final Image output = new Image();
			output.setType(type);
			output.setDetails(details);
			return output;
		}

		public ImageBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public ImageBuilder withDetail(final String key, final Object value) {
			this.details.put(key, value);
			return this;
		}

		public ImageBuilder withDetails(final Map<String, Object> details) {
			this.details.putAll(details);
			return this;
		}

	}

	public static class LongIdAndDescriptionBuilder extends ModelBuilder<LongIdAndDescription> {

		private Long id;
		private String description;

		private LongIdAndDescriptionBuilder() {
			// use factory method
		}

		@Override
		protected LongIdAndDescription doBuild() {
			final LongIdAndDescription output = new LongIdAndDescription();
			output.setId(id);
			output.setDescription(description);
			return output;
		}

		public LongIdAndDescriptionBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public LongIdAndDescriptionBuilder withDescription(final String description) {
			this.description = description;
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
		private static final Map<Long, Reference> NO_REFERENCES = emptyMap();

		private Long total;
		private Map<Long, Long> positions;
		private Map<Long, Reference> references;

		private MetadataBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			super.doValidate();
			positions = defaultIfNull(positions, NO_POSITIONS);
			references = defaultIfNull(references, NO_REFERENCES);
		}

		@Override
		protected DetailResponseMetadata doBuild() {
			final DetailResponseMetadata output = new DetailResponseMetadata();
			output.setTotal(total);
			output.setPositions(positions);
			output.setReferences(references);
			return output;
		}

		public MetadataBuilder withTotal(final Long total) {
			this.total = total;
			return this;
		}

		public MetadataBuilder withTotal(final Integer total) {
			this.total = total.longValue();
			return this;
		}

		public MetadataBuilder withPositions(final Map<Long, Long> positions) {
			this.positions = positions;
			return this;
		}

		public MetadataBuilder withReferences(final Map<Long, Reference> references) {
			this.references = references;
			return this;
		}

	}

	public static class NodeBuilder extends ModelBuilder<Node> {

		private Long id;
		private Long parent;
		private final Map<String, Object> metadata = newHashMap();

		private NodeBuilder() {
			// use factory method
		}

		@Override
		protected Node doBuild() {
			final Node output = new Node();
			output.setId(id);
			output.setParent(parent);
			output.setMetadata(metadata);
			return output;
		}

		public NodeBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public NodeBuilder withParent(final Long parent) {
			this.parent = parent;
			return this;
		}

		public NodeBuilder withMetadata(final String key, final Object value) {
			metadata.put(key, value);
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

	public static class ProcessInstancePrivilegesBuilder extends ModelBuilder<ProcessInstancePrivileges> {

		private boolean stoppable;

		private ProcessInstancePrivilegesBuilder() {
			// use factory method
		}

		@Override
		protected ProcessInstancePrivileges doBuild() {
			final ProcessInstancePrivileges output = new ProcessInstancePrivileges();
			output.setStoppable(stoppable);
			return output;
		}

		public ProcessInstancePrivilegesBuilder stoppable(final boolean stoppable) {
			this.stoppable = stoppable;
			return this;
		}
	}

	public static class ProcessInstanceAdvanceBuilder extends ModelBuilder<ProcessInstanceAdvanceable> {

		private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
		private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

		private static final Map<String, Object> EMPTY_MAP = emptyMap();
		private static final Values NO_VALUES = newValues().withValues(EMPTY_MAP).build();
		private static final Collection<Widget> NO_WIDGETS = emptyList();

		private String type;
		private Long id;
		private String name;
		private Long status;
		private Values values;
		private String activityId;
		private Boolean advance;
		private Collection<Widget> widgets;

		private ProcessInstanceAdvanceBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			values = defaultIfNull(values, NO_VALUES);
			advance = defaultIfNull(advance, FALSE);
			widgets = defaultIfNull(widgets, NO_WIDGETS);
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
			output.setWidgets(widgets);
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
			this.values = newValues() //
					.withValues(values) //
					.build();
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

		public ProcessInstanceAdvanceBuilder withWidgets(final Collection<Widget> widgets) {
			this.widgets = widgets;
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
		private Collection<AttributeOrder> defaultOrder;
		private String parent;

		private ProcessWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
			statuses = defaultIfNull(statuses, NO_STATUSES);
			defaultOrder = defaultIfNull(defaultOrder, NO_ORDER);
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
			output.setDefaultOrder(defaultOrder);
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

		public ProcessWithFullDetailsBuilder withDefaultOrder(final Collection<AttributeOrder> defaultOrder) {
			this.defaultOrder = defaultOrder;
			return this;
		}

		public ProcessWithFullDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class ReferenceBuilder extends ModelBuilder<Reference> {

		private String description;
		private Long parent;

		@Override
		protected Reference doBuild() {
			final Reference output = new Reference();
			output.setDescription(description);
			output.setParent(parent);
			return output;
		}

		public ReferenceBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ReferenceBuilder withParent(final Long parent) {
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

	public static class ReportBuilder extends ModelBuilder<Report> {

		private Long id;
		private String title;
		private String description;

		@Override
		protected Report doBuild() {
			final Report output = new Report();
			output.setId(id);
			output.setTitle(title);
			output.setDescription(description);
			return output;
		}

		public ReportBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public ReportBuilder withTitle(final String title) {
			this.title = title;
			return this;
		}

		public ReportBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class ResponseSingleBuilder<T> extends ModelBuilder<ResponseSingle<T>> {

		private T element;
		private DetailResponseMetadata metadata;

		private ResponseSingleBuilder() {
			// use factory method
		}

		@Override
		protected ResponseSingle<T> doBuild() {
			final ResponseSingle<T> output = new ResponseSingle<T>();
			output.setElement(element);
			output.setMetadata(metadata);
			return output;
		}

		public ResponseSingleBuilder<T> withElement(final T element) {
			this.element = element;
			return this;
		}

		public ResponseSingleBuilder<T> withMetadata(final DetailResponseMetadata metadata) {
			this.metadata = metadata;
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
		private Object output;

		private WidgetBuilder() {
			// use factory method
		}

		@Override
		protected Widget doBuild() {
			final Widget _output = new Widget();
			_output.setId(id);
			_output.setType(type);
			_output.setActive(active);
			_output.setRequired(required);
			_output.setLabel(label);
			_output.setData(data);
			_output.setOutput(output);
			return _output;
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

		public WidgetBuilder withOutput(final Object output) {
			this.output = output;
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

	public static AttributeOrderBuilder newAttributeOrder() {
		return new AttributeOrderBuilder();
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

	public static DomainTreeBuilder newDomainTree() {
		return new DomainTreeBuilder();
	}

	public static DomainWithBasicDetailsBuilder newDomainWithBasicDetails() {
		return new DomainWithBasicDetailsBuilder();
	}

	public static DomainWithFullDetailsBuilder newDomainWithFullDetails() {
		return new DomainWithFullDetailsBuilder();
	}

	public static EmailBuilder newEmail() {
		return new EmailBuilder();
	}

	public static EmailTemplateBuilder newEmailTemplate() {
		return new EmailTemplateBuilder();
	}

	public static FileSystemObjectBuilder newFileSystemObject() {
		return new FileSystemObjectBuilder();
	}

	public static FilterBuilder newFilter() {
		return new FilterBuilder();
	}

	public static FunctionWithBasicDetailsBuilder newFunctionWithBasicDetails() {
		return new FunctionWithBasicDetailsBuilder();
	}

	public static FunctionWithFullDetailsBuilder newFunctionWithFullDetails() {
		return new FunctionWithFullDetailsBuilder();
	}

	public static GraphConfigurationBuilder newGraphConfiguration() {
		return new GraphConfigurationBuilder();
	}

	public static IconBuilder newIcon() {
		return new IconBuilder();
	}

	public static ImageBuilder newImage() {
		return new ImageBuilder();
	}

	public static LongIdBuilder newLongId() {
		return new LongIdBuilder();
	}

	public static LongIdAndDescriptionBuilder newLongIdAndDescription() {
		return new LongIdAndDescriptionBuilder();
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

	public static NodeBuilder newNode() {
		return new NodeBuilder();
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

	public static ProcessInstancePrivilegesBuilder newProcessInstancePrivileges() {
		return new ProcessInstancePrivilegesBuilder();
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

	public static ReferenceBuilder newReference() {
		return new ReferenceBuilder();
	}

	public static RelationBuilder newRelation() {
		return new RelationBuilder();
	}

	public static ReportBuilder newReport() {
		return new ReportBuilder();
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
