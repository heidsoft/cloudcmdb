package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.logic.data.lookup.LookupLogic.UNUSED_LOOKUP_QUERY;
import static org.cmdbuild.logic.data.lookup.LookupLogic.UNUSED_LOOKUP_TYPE_QUERY;

import java.util.Iterator;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.slf4j.Logger;

@Deprecated
public class DefaultCardDiffer implements CardDiffer {

	private final CMDataView dataView;
	private final LookupLogic lookupLogic;
	private final MapperRules support;
	private final Logger logger = org.cmdbuild.bim.logging.LoggingSupport.logger;

	public DefaultCardDiffer(final CMDataView dataView, final LookupLogic lookupLogic, final MapperRules support) {
		this.dataView = dataView;
		this.lookupLogic = lookupLogic;
		this.support = support;
	}

	@Override
	public CMCard updateCard(final Entity sourceEntity, final CMCard oldCard) {
		CMCard updatedCard = null;
		final CMClass theClass = oldCard.getType();
		final String className = theClass.getName();
		if (!className.equals(sourceEntity.getTypeName())) {
			// better safe than sorry...
			return updatedCard;
		}

		final CMCardDefinition cardDefinition = dataView.update(oldCard);
		final Iterable<? extends CMAttribute> attributes = theClass.getAttributes();
		logger.info("Updating card " + oldCard.getId() + " of type " + className);
		boolean sendDelta = false;

		for (final CMAttribute attribute : attributes) {
			final String attributeName = attribute.getName();

			final CMAttributeType<?> attributeType = attribute.getType();
			final boolean isReference = attributeType instanceof ReferenceAttributeType;
			final boolean isLookup = attributeType instanceof LookupAttributeType;
			final Object oldAttributeValue = oldCard.get(attributeName);

			if (sourceEntity.getAttributeByName(attributeName).isValid()) {
				if (isReference || isLookup) {
					final IdAndDescription oldReference = (IdAndDescription) oldAttributeValue;
					Long newReferencedId = null;
					if (isReference) {
						final String referencedClass = findReferencedClassNameFromReferenceAttribute(attribute);
						final String newReferencedKey = sourceEntity.getAttributeByName(attributeName).getValue();
						newReferencedId = support.findIdFromKey(newReferencedKey, referencedClass, dataView);
					} else if (isLookup) {
						final String lookupType = ((LookupAttributeType) attribute.getType()).getLookupTypeName();
						final String newLookupValue = sourceEntity.getAttributeByName(attributeName).getValue();
						newReferencedId = findLookupIdFromDescription(newLookupValue, lookupType);
					}
					if (newReferencedId != null && !newReferencedId.equals(oldReference.getId())) {
						final IdAndDescription newReference = new IdAndDescription(newReferencedId, "");
						cardDefinition.set(attributeName, newReference);
						sendDelta = true;
					}
				} else {
					final Object newAttributeValue = attributeType.convertValue(sourceEntity.getAttributeByName(
							attributeName).getValue());
					if ((newAttributeValue != null && !newAttributeValue.equals(oldAttributeValue))
							|| (newAttributeValue == null && oldAttributeValue != null)) {
						cardDefinition.set(attributeName, newAttributeValue);
						sendDelta = true;
					}
				}
			}
		}
		if (sendDelta) {
			updatedCard = cardDefinition.save();
			logger.info("Card updated");
		}
		return updatedCard;
	}

	@Override
	public CMCard createCard(final Entity sourceEntity) {
		CMCard newCard = null;
		final String className = sourceEntity.getTypeName();
		final CMClass theClass = dataView.findClass(className);
		if (theClass == null) {
			logger.warn("Class " + className + " not found");
			return null;
		}
		final CMCardDefinition cardDefinition = dataView.createCardFor(theClass);

		final Iterable<? extends CMAttribute> attributes = theClass.getAttributes();
		logger.info("Building card of type " + className);
		boolean sendDelta = false;

		// FIXME Da ottimizzare!!!
		for (final CMAttribute attribute : attributes) {
			final String attributeName = attribute.getName();
			final boolean isReference = attribute.getType() instanceof ReferenceAttributeType;
			final boolean isLookup = attribute.getType() instanceof LookupAttributeType;
			final Attribute sourceAttribute = sourceEntity.getAttributeByName(attributeName);
			if (sourceAttribute.isValid()) {
				if (isReference || isLookup) {
					Long newReferencedId = null;
					if (isReference) {
						final String referencedClass = findReferencedClassNameFromReferenceAttribute(attribute);
						final String referencedGuid = sourceAttribute.getValue();
						newReferencedId = support.findIdFromKey(referencedGuid, referencedClass, dataView);
					} else if (isLookup) {
						final String newLookupValue = sourceAttribute.getValue();
						final String lookupType = ((LookupAttributeType) attribute.getType()).getLookupTypeName();
						newReferencedId = findLookupIdFromDescription(newLookupValue, lookupType);
					}
					if (newReferencedId != null) {
						sourceAttribute.setValue(newReferencedId.toString());
						cardDefinition.set(attributeName, sourceAttribute.getValue());
						sendDelta = true;
					}
				} else {
					cardDefinition.set(attributeName, sourceAttribute.getValue());
					sendDelta = true;
				}
			}
		}
		if (sendDelta) {
			newCard = cardDefinition.save();
		}
		return newCard;
	}

	private String findReferencedClassNameFromReferenceAttribute(final CMAttribute attribute) {
		final String domainName = ((ReferenceAttributeType) attribute.getType()).getDomainName();
		final CMDomain domain = dataView.findDomain(domainName);
		String referencedClass = "";
		final String ownerClassName = attribute.getOwner().getName();
		if (domain.getClass1().getName().equals(ownerClassName)) {
			referencedClass = domain.getClass2().getName();
		} else {
			referencedClass = domain.getClass1().getName();
		}
		return referencedClass;
	}

	private Long findLookupIdFromDescription(final String lookupValue, final String lookupType) {
		Long lookupId = null;
		final Iterable<LookupType> allLookupTypes = lookupLogic.getAllTypes(UNUSED_LOOKUP_TYPE_QUERY);
		LookupType theType = null;
		for (final Iterator<LookupType> it = allLookupTypes.iterator(); it.hasNext();) {
			final LookupType lt = it.next();
			if (lt.name.equals(lookupType)) {
				theType = lt;
				break;
			}
		}
		final Iterable<Lookup> allLookusOfType = lookupLogic.getAllLookup(theType, true, UNUSED_LOOKUP_QUERY);

		for (final Iterator<Lookup> it = allLookusOfType.iterator(); it.hasNext();) {
			final Lookup l = it.next();
			if (l.getDescription() != null && l.getDescription().equals(lookupValue)) {
				lookupId = l.getId();
				break;
			}
		}
		return lookupId;
	}

}
