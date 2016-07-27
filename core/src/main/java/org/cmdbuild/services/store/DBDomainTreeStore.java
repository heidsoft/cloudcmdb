package org.cmdbuild.services.store;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static org.cmdbuild.dao.guava.Functions.toCard;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.as;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.domainTree.DomainTreeNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class DBDomainTreeStore {

	private enum Attributes {
		/*
		 * TODO BASE_NODE is a specific field of the GIS navigation tree for the
		 * moment the field is left here for not touch all the GIS module but
		 * the column redundant because define only one node and only in the GIS
		 * navigation Tree
		 */
		BASE_NODE("BaseNode"), //
		DESCRIPTION("Description"), //
		DIRECT("Direct"), //
		DOMAIN_NAME("DomainName"), //
		ENABLE_RECURSION("EnableRecursion"), //
		FILTER("TargetFilter"), //
		ID_GROUP("IdGroup"), //
		ID_PARENT("IdParent"), //
		TARGET_CLASS_DESCRIPTION("TargetClassDescription"), //
		TARGET_CLASS_NAME("TargetClassName"), //
		TYPE("Type");

		private String name;

		Attributes(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private static final Function<CMCard, DomainTreeNode> CARD_TO_DOMAIN_TREE_NODE = new Function<CMCard, DomainTreeNode>() {

		@Override
		public DomainTreeNode apply(final CMCard card) {
			final DomainTreeNode domainTreeNode = new DomainTreeNode();
			domainTreeNode.setId(card.getId());
			domainTreeNode.setDescription((String) card.get(Attributes.DESCRIPTION.getName()));
			domainTreeNode.setDirect(booleanCast(card.get(Attributes.DIRECT.getName())));
			domainTreeNode.setDomainName((String) card.get(Attributes.DOMAIN_NAME.getName()));
			domainTreeNode.setType((String) card.get(Attributes.TYPE.getName()));
			domainTreeNode.setIdGroup(safeLongCast(card.get(Attributes.ID_GROUP.getName())));
			domainTreeNode.setIdParent(safeLongCast(card.get(Attributes.ID_PARENT.getName())));
			domainTreeNode.setTargetClassDescription((String) card.get(Attributes.TARGET_CLASS_DESCRIPTION.getName()));
			domainTreeNode.setTargetClassName(((String) card.get(Attributes.TARGET_CLASS_NAME.getName())));
			domainTreeNode.setBaseNode((booleanCast(card.get(Attributes.BASE_NODE.getName()))));
			domainTreeNode.setTargetFilter((String) card.get(Attributes.FILTER.getName()));
			domainTreeNode.setEnableRecursion((booleanCast(card.get(Attributes.ENABLE_RECURSION.getName()))));
			return domainTreeNode;
		}

		/*
		 * getValue method of a Card returns an Object. For the Ids it returns
		 * an Integer but we want a Long. Cast them ignoring null values
		 */
		private Long safeLongCast(final Object o) {
			if (o == null) {
				return null;
			} else if (o instanceof Long) {
				return (Long) o;
			} else if (o instanceof Integer) {
				return ((Integer) o).longValue();
			}
			return null;
		}

		private boolean booleanCast(final Object o) {
			if (o == null) {
				return false;
			} else {
				return (Boolean) o;
			}
		}

	};

	private static final String TABLE_NAME = "_DomainTreeNavigation";
	private static final Alias T = name("A");

	private final CMDataView dataView;

	public DBDomainTreeStore(final CMDataView dataView) {
		this.dataView = dataView;
	}

	public void createOrReplaceTree(final String treeType, final String description, final DomainTreeNode root) {
		removeTree(treeType);
		saveNode(treeType, description, root);
	}

	public void removeTree(final String treeType) {
		for (final CMCard element : from(dataView //
				.select(anyAttribute(T)) //
				.from(target(), as(T)) //
				.where(condition(attribute(T, Attributes.TYPE.getName()), eq(treeType))) //
				.run()) //
						.transform(toCard(T))) {
			dataView.delete(element);
		}

	}

	public Map<String, String> getTreeNames(final Predicate<DomainTreeNode> predicate) {
		final Map<String, String> names = newHashMap();
		for (final DomainTreeNode element : from(dataView //
				.select(anyAttribute(T)) //
				.from(target(), as(T)) //
				.run()) //
						.transform(toCard(T)) //
						.transform(CARD_TO_DOMAIN_TREE_NODE) //
						.filter(predicate)) {
			names.put(element.getType(), element.getDescription());
		}
		return names;
	}

	public DomainTreeNode getDomainTree(final String type) {
		DomainTreeNode root = null;
		final Map<Long, DomainTreeNode> nodesById = newHashMap();
		for (final DomainTreeNode current : from(dataView //
				.select(anyAttribute(T)) //
				.from(target(), as(T)) //
				.where(condition(attribute(T, Attributes.TYPE.getName()), eq(type))) //
				.run()) //
						.transform(toCard(T)) //
						.transform(CARD_TO_DOMAIN_TREE_NODE)) {
			for (final DomainTreeNode element : nodesById.values()) {
				// Link children to current node
				if (element.getIdParent() != null && element.getIdParent().equals(current.getId())) {
					current.addChildNode(element);
				}
			}

			// link the currentNode as child of a node if already created
			if (current.getIdParent() != null) {
				final DomainTreeNode maybeParent = nodesById.get(current.getIdParent());
				if (maybeParent != null) {
					maybeParent.addChildNode(current);
				}
			} else {
				root = current;
			}

			nodesById.put(current.getId(), current);
		}
		return root;
	}

	private void saveNode(final String treeType, final String description, final DomainTreeNode root) {
		final Long id = dataView.createCardFor(target()) //
				.set(Attributes.DIRECT.getName(), root.isDirect()) //
				.set(Attributes.DOMAIN_NAME.getName(), root.getDomainName()) //
				.set(Attributes.TYPE.getName(), treeType) //
				.set(Attributes.ID_GROUP.getName(), root.getIdGroup()) //
				.set(Attributes.ID_PARENT.getName(), root.getIdParent()) //
				.set(Attributes.TARGET_CLASS_NAME.getName(), root.getTargetClassName()) //
				.set(Attributes.TARGET_CLASS_DESCRIPTION.getName(), root.getTargetClassDescription()) //
				.set(Attributes.FILTER.getName(), root.getTargetFilter()) //
				.set(Attributes.DESCRIPTION.getName(), description) //
				.set(Attributes.BASE_NODE.getName(), root.isBaseNode()) //
				.set(Attributes.ENABLE_RECURSION.getName(), root.isEnableRecursion()) //
				.save() //
				.getId();
		for (final DomainTreeNode child : root.getChildNodes()) {
			child.setIdParent(id);
			saveNode(treeType, description, child);
		}
	}

	private CMClass target() {
		return dataView.findClass(TABLE_NAME);
	}

}
