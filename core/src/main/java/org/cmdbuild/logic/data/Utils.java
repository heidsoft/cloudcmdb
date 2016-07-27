package org.cmdbuild.logic.data;

import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.workflow.CMProcessClass;
import org.joda.time.DateTime;

public class Utils {

	public static abstract class CMAttributeWrapper implements CMAttributeDefinition {

		private final CMAttribute delegate;

		protected CMAttributeWrapper(final CMAttribute delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public CMEntryType getOwner() {
			return delegate.getOwner();
		}

		@Override
		public CMAttributeType<?> getType() {
			return delegate.getType();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

		@Override
		public String getDefaultValue() {
			return delegate.getDefaultValue();
		}

		@Override
		public Boolean isDisplayableInList() {
			return delegate.isDisplayableInList();
		}

		@Override
		public boolean isMandatory() {
			return delegate.isMandatory();
		}

		@Override
		public boolean isUnique() {
			return delegate.isUnique();
		}

		@Override
		public Boolean isActive() {
			return delegate.isActive();
		}

		@Override
		public Mode getMode() {
			return delegate.getMode();
		}

		@Override
		public Integer getIndex() {
			return delegate.getIndex();
		}

		@Override
		public String getGroup() {
			return delegate.getGroup();
		}

		@Override
		public Integer getClassOrder() {
			return delegate.getClassOrder();
		}

		@Override
		public String getEditorType() {
			return delegate.getEditorType();
		}

		@Override
		public String getFilter() {
			return delegate.getFilter();
		}

		@Override
		public String getForeignKeyDestinationClassName() {
			return delegate.getForeignKeyDestinationClassName();
		}

	}

	public static CMClassDefinition definitionForNew(final EntryType entryType, final CMClass parentClass) {
		return new CMClassDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return fromName(entryType);
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public String getDescription() {
				return entryType.getDescription();
			}

			@Override
			public CMClass getParent() {
				return parentClass;
			}

			@Override
			public boolean isSuperClass() {
				return entryType.isSuperClass();
			}

			@Override
			public boolean isHoldingHistory() {
				return entryType.isHoldingHistory();
			}

			@Override
			public boolean isActive() {
				return entryType.isActive();
			}

			@Override
			public boolean isUserStoppable() {
				return entryType.isUserStoppable();
			}

			@Override
			public boolean isSystem() {
				return entryType.isSystem();
			}

		};
	}

	private static CMIdentifier fromName(final EntryType entryType) {
		return new CMIdentifier() {

			@Override
			public String getLocalName() {
				return entryType.getName();
			}

			@Override
			public String getNameSpace() {
				return entryType.getNamespace();
			}

		};
	}

	public static CMClassDefinition definitionForExisting(final EntryType clazz, final CMClass existingClass) {
		return new CMClassDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return existingClass.getIdentifier();
			}

			@Override
			public Long getId() {
				return existingClass.getId();
			}

			@Override
			public String getDescription() {
				return clazz.getDescription();
			}

			@Override
			public CMClass getParent() {
				return existingClass.getParent();
			}

			@Override
			public boolean isSuperClass() {
				return existingClass.isSuperclass();
			}

			@Override
			public boolean isHoldingHistory() {
				return existingClass.holdsHistory();
			}

			@Override
			public boolean isActive() {
				return clazz.isActive();
			}

			@Override
			public boolean isUserStoppable() {
				return clazz.isUserStoppable();
			}

			@Override
			public boolean isSystem() {
				return existingClass.isSystem();
			}

		};
	}

	public static CMClassDefinition unactive(final CMClass existingClass) {
		return new CMClassDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return existingClass.getIdentifier();
			}

			@Override
			public Long getId() {
				return existingClass.getId();
			}

			@Override
			public String getDescription() {
				return existingClass.getDescription();
			}

			@Override
			public CMClass getParent() {
				return existingClass.getParent();
			}

			@Override
			public boolean isSuperClass() {
				return existingClass.isSuperclass();
			}

			@Override
			public boolean isHoldingHistory() {
				return existingClass.holdsHistory();
			}

			@Override
			public boolean isActive() {
				return false;
			}

			@Override
			public boolean isUserStoppable() {
				final boolean userStoppable;
				if (existingClass instanceof CMProcessClass) {
					userStoppable = CMProcessClass.class.cast(existingClass).isUserStoppable();
				} else {
					userStoppable = false;
				}
				return userStoppable;
			}

			@Override
			public boolean isSystem() {
				return existingClass.isSystem();
			}

		};
	}

	public static CMAttributeDefinition definitionForNew(final Attribute attribute, final CMEntryType owner) {
		return new CMAttributeDefinition() {

			@Override
			public String getName() {
				return attribute.getName();
			}

			@Override
			public CMEntryType getOwner() {
				return owner;
			}

			@Override
			public CMAttributeType<?> getType() {
				return attribute.getType();
			}

			@Override
			public String getDescription() {
				return attribute.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return attribute.getDefaultValue();
			}

			@Override
			public Boolean isDisplayableInList() {
				return attribute.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return attribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attribute.isUnique();
			}

			@Override
			public Boolean isActive() {
				return attribute.isActive();
			}

			@Override
			public Mode getMode() {
				return attribute.getMode();
			}

			@Override
			public Integer getIndex() {
				return attribute.getIndex();
			}

			@Override
			public String getGroup() {
				return attribute.getGroup();
			}

			@Override
			public Integer getClassOrder() {
				return attribute.getClassOrder();
			}

			@Override
			public String getEditorType() {
				return attribute.getEditorType();
			}

			@Override
			public String getFilter() {
				return attribute.getFilter();
			}

			@Override
			public String getForeignKeyDestinationClassName() {
				return attribute.getForeignKeyDestinationClassName();
			}

		};
	}

	public static CMAttributeDefinition definitionForExisting(final CMAttribute delegate, final Attribute attribute) {
		return new CMAttributeWrapper(delegate) {

			@Override
			/*
			 * Some info about the attributes are stored in the CMAttributeType,
			 * so for String, Lookup and Decimal use the new attribute type to
			 * update these info
			 */
			public CMAttributeType<?> getType() {
				if (delegate.getType() instanceof LookupAttributeType
						&& attribute.getType() instanceof LookupAttributeType) {
					return attribute.getType();
				} else if (delegate.getType() instanceof StringAttributeType
						&& attribute.getType() instanceof StringAttributeType) {
					return attribute.getType();
				} else if (delegate.getType() instanceof DecimalAttributeType
						&& attribute.getType() instanceof DecimalAttributeType) {
					return attribute.getType();
				} else if (delegate.getType() instanceof IpAddressAttributeType
						&& attribute.getType() instanceof IpAddressAttributeType) {
					return attribute.getType();
				} else {
					return delegate.getType();
				}
			}

			@Override
			public String getDescription() {
				return notEqual(super.getDescription(), attribute.getDescription()) ? attribute.getDescription() : null;
			}

			@Override
			public Boolean isDisplayableInList() {
				return notEqual(super.isDisplayableInList(), attribute.isDisplayableInList()) ? attribute
						.isDisplayableInList() : null;
			}

			@Override
			public boolean isMandatory() {
				return attribute.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return attribute.isUnique();
			}

			@Override
			public Boolean isActive() {
				return notEqual(super.isActive(), attribute.isActive()) ? attribute.isActive() : null;
			}

			@Override
			public Mode getMode() {
				return notEqual(super.getMode(), attribute.getMode()) ? attribute.getMode() : null;
			}

			@Override
			public String getGroup() {
				return notEqual(super.getGroup(), attribute.getGroup()) ? attribute.getGroup() : null;
			}

			@Override
			public Integer getIndex() {
				// not changed
				return null;
			}

			@Override
			public Integer getClassOrder() {
				// not changed
				return null;
			}

			@Override
			public String getEditorType() {
				return notEqual(super.getEditorType(), attribute.getEditorType()) ? attribute.getEditorType() : null;
			}

			@Override
			public String getFilter() {
				return notEqual(super.getFilter(), attribute.getFilter()) ? attribute.getFilter() : null;
			}

		};
	}

	public static CMAttributeDefinition withIndex(final CMAttribute delegate, final int index) {
		return new CMAttributeWrapper(delegate) {

			@Override
			public String getDescription() {
				// not changed
				return null;
			}

			@Override
			public Boolean isDisplayableInList() {
				// not changed
				return null;
			}

			@Override
			public boolean isMandatory() {
				return delegate.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return delegate.isUnique();
			}

			@Override
			public Boolean isActive() {
				// not changed
				return null;
			}

			@Override
			public Mode getMode() {
				// not changed
				return null;
			}

			@Override
			public String getGroup() {
				// not changed
				return null;
			}

			@Override
			public Integer getClassOrder() {
				// not changed
				return null;
			}

			@Override
			public String getEditorType() {
				// not changed
				return null;
			}

			@Override
			public String getFilter() {
				// not changed
				return null;
			}

			@Override
			public Integer getIndex() {
				return index;
			}

		};
	}

	public static CMAttributeDefinition withClassOrder(final CMAttribute delegate, final int classOrder) {
		return new CMAttributeWrapper(delegate) {

			@Override
			public String getDescription() {
				// not changed
				return null;
			}

			@Override
			public Boolean isDisplayableInList() {
				// not changed
				return null;
			}

			@Override
			public boolean isMandatory() {
				return delegate.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return delegate.isUnique();
			}

			@Override
			public Boolean isActive() {
				// not changed
				return null;
			}

			@Override
			public Mode getMode() {
				// not changed
				return null;
			}

			@Override
			public String getGroup() {
				// not changed
				return null;
			}

			@Override
			public String getEditorType() {
				// not changed
				return null;
			}

			@Override
			public String getFilter() {
				// not changed
				return null;
			}

			@Override
			public Integer getIndex() {
				// not changed
				return null;
			}

			@Override
			public Integer getClassOrder() {
				return classOrder;
			}

		};
	}

	public static CMAttributeDefinition unactive(final CMAttribute delegate) {
		return new CMAttributeWrapper(delegate) {

			@Override
			public Boolean isActive() {
				return false;
			}

		};
	}

	public static CMDomainDefinition definitionForNew(final Domain domain, final CMClass class1, final CMClass class2) {
		return new CMDomainDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return fromName(domain);
			}

			@Override
			public Long getId() {
				return null;
			}

			@Override
			public CMClass getClass1() {
				return class1;
			}

			@Override
			public CMClass getClass2() {
				return class2;
			}

			@Override
			public String getDescription() {
				return domain.getDescription();
			}

			@Override
			public String getDirectDescription() {
				return domain.getDirectDescription();
			}

			@Override
			public String getInverseDescription() {
				return domain.getInverseDescription();
			}

			@Override
			public String getCardinality() {
				return domain.getCardinality();
			}

			@Override
			public boolean isMasterDetail() {
				return domain.isMasterDetail();
			}

			@Override
			public String getMasterDetailDescription() {
				return domain.getMasterDetailDescription();
			}

			@Override
			public boolean isActive() {
				return domain.isActive();
			}

			@Override
			public Iterable<String> getDisabled1() {
				return domain.getDisabled1();
			}

			@Override
			public Iterable<String> getDisabled2() {
				return domain.getDisabled2();
			}

		};
	}

	private static CMIdentifier fromName(final Domain domain) {
		return new CMIdentifier() {

			@Override
			public String getLocalName() {
				return domain.getName();
			}

			@Override
			public String getNameSpace() {
				// TODO must be done ASAP
				return null;
			}

		};
	}

	public static CMDomainDefinition definitionForExisting(final Domain domainWithChanges, final CMDomain existing) {
		return new CMDomainDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return existing.getIdentifier();
			}

			@Override
			public Long getId() {
				return existing.getId();
			}

			@Override
			public CMClass getClass1() {
				return existing.getClass1();
			}

			@Override
			public CMClass getClass2() {
				return existing.getClass2();
			}

			@Override
			public String getDescription() {
				return domainWithChanges.getDescription();
			}

			@Override
			public String getDirectDescription() {
				return domainWithChanges.getDirectDescription();
			}

			@Override
			public String getInverseDescription() {
				return domainWithChanges.getInverseDescription();
			}

			@Override
			public String getCardinality() {
				return existing.getCardinality();
			}

			@Override
			public boolean isMasterDetail() {
				return domainWithChanges.isMasterDetail();
			}

			@Override
			public String getMasterDetailDescription() {
				return domainWithChanges.getMasterDetailDescription();
			}

			@Override
			public boolean isActive() {
				return domainWithChanges.isActive();
			}

			@Override
			public Iterable<String> getDisabled1() {
				return domainWithChanges.getDisabled1();
			}

			@Override
			public Iterable<String> getDisabled2() {
				return domainWithChanges.getDisabled2();
			}

		};
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return an empty String, otherwise cast the object to string
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 * 
	 * @deprecated use {@link StringUtils} functions.
	 */
	@Deprecated
	public static String readString(final CMCard card, final String attributeName) {
		final Object value = card.get(attributeName);
		final String output;
		if (value == null) {
			output = EMPTY;
		} else {
			output = (String) value;
		}
		return output;
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return an false, otherwise cast the object to boolean
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 * 
	 * @deprecated use {@link BooleanUtils} functions.
	 */
	@Deprecated
	public static boolean readBoolean(final CMCard card, final String attributeName) {
		final Object value = card.get(attributeName);
		final boolean output;
		if (value == null) {
			output = false;
		} else {
			output = (Boolean) value;
		}
		return output;
	}

	/**
	 * Read from the given card the attribute with the given name. If null
	 * return null, otherwise try to cast the object to org.joda.time.DateTime
	 * 
	 * @param card
	 * @param attributeName
	 * @return
	 */
	public static DateTime readDateTime(final CMCard card, final String attributeName) {
		final Object value = card.get(attributeName);

		if (value instanceof DateTime) {
			return (DateTime) value;
		} else if (value instanceof java.sql.Date) {
			return new DateTime(((java.util.Date) value).getTime());
		}

		return null;
	}

	private Utils() {
		// prevents instantiation
	}

}
