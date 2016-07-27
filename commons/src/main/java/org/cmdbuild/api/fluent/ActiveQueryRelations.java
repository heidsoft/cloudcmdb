package org.cmdbuild.api.fluent;

import java.util.ArrayList;
import java.util.List;

public class ActiveQueryRelations extends RelationsQuery {

	private final FluentApiExecutor executor;

	ActiveQueryRelations(final FluentApiExecutor executor, final String className, final Integer id) {
		super(className, id);
		this.executor = executor;
	}

	protected FluentApiExecutor executor() {
		return executor;
	}

	public ActiveQueryRelations withDomain(final String domainName) {
		setDomainName(domainName);
		return this;
	}

	public List<CardDescriptor> fetch() {
		final List<CardDescriptor> descriptors = new ArrayList<CardDescriptor>();
		final List<Relation> relations = executor().fetch(this);
		for (final Relation relation : relations) {
			descriptors.add(descriptorFrom(relation));
		}
		return descriptors;
	}

	private CardDescriptor descriptorFrom(final Relation relation) {
		final String className;
		final int id;
		if (getCardId() == relation.getCardId1()) {
			className = relation.getClassName2();
			id = relation.getCardId2();
		} else {
			className = relation.getClassName1();
			id = relation.getCardId1();
		}
		return new CardDescriptor(className, id);
	}

}
