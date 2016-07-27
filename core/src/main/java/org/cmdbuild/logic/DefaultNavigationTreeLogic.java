package org.cmdbuild.logic;

import java.util.Map;

import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.services.store.DBDomainTreeStore;

import com.google.common.base.Predicate;

public class DefaultNavigationTreeLogic implements NavigationTreeLogic {

	private final DBDomainTreeStore domainTreeStore;

	public DefaultNavigationTreeLogic(final DBDomainTreeStore domainTreeStore) {
		this.domainTreeStore = domainTreeStore;
	}

	@Override
	public void create(final String name, final String description, final boolean active, final DomainTreeNode root) {
		domainTreeStore.createOrReplaceTree(name, description, root);
	}

	@Override
	public void save(final String name, final String description, final boolean active, final DomainTreeNode root) {
		domainTreeStore.createOrReplaceTree(name, description, root);
	}

	@Override
	public Map<String, String> get(final Predicate<DomainTreeNode> predicate) {
		return domainTreeStore.getTreeNames(predicate);
	}

	@Override
	public DomainTreeNode getTree(final String name) {
		return domainTreeStore.getDomainTree(name);
	}

	@Override
	public void delete(final String name) {
		domainTreeStore.removeTree(name);
	}

}
