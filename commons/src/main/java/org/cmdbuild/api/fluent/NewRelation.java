package org.cmdbuild.api.fluent;

public class NewRelation extends ActiveRelation {

	NewRelation(final FluentApiExecutor executor, final String domainName) {
		super(executor, domainName);
	}

	public NewRelation withCard1(final String className, final int cardId) {
		super.setCard1(className, cardId);
		return this;
	}

	public NewRelation withCard2(final String className, final int cardId) {
		super.setCard2(className, cardId);
		return this;
	}

	public void create() {
		executor().create(this);
	}

}
