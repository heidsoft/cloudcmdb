package org.cmdbuild.logic.data.access;

import org.cmdbuild.model.data.Card;

public class CMCardWithPosition extends Card {

	private final Card delegate;
	private final Long position;

	public CMCardWithPosition(final Card delegate, final Long position) {
		super(builder(delegate));
		this.delegate = delegate;
		this.position = position;
	}

	private static Builder builder(final Card delegate) {
		final Builder builder = Card.newInstance();
		return (delegate == null) ? builder : builder.clone(delegate);
	}

	public boolean isFound() {
		return (delegate != null);
	}

	public Long getPosition() {
		return position;
	}

	public boolean hasNoPosition() {
		return position < 0;
	}

}