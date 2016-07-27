package org.cmdbuild.logic;

import java.util.Map;

import org.cmdbuild.model.domainTree.DomainTreeNode;

import com.google.common.base.Predicate;

public interface NavigationTreeLogic extends Logic {

	void create(final String name, final String description, final boolean active, DomainTreeNode deserialize);

	void save(final String name, final String description, final boolean active, DomainTreeNode deserialize);

	Map<String, String> get(Predicate<DomainTreeNode> predicate);

	DomainTreeNode getTree(final String name);

	void delete(final String name);

}
