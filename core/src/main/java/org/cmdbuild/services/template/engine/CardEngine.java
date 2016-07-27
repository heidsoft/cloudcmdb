package org.cmdbuild.services.template.engine;

import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;

public class CardEngine implements Engine {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<CardEngine> {

		private CMCard card;

		private Builder() {
			// use factory method
		}

		@Override
		public CardEngine build() {
			validate();
			return new CardEngine(this);
		}

		private void validate() {
			Validate.notNull(card, "missing '{}'", CMCard.class);
		}

		public Builder withCard(final CMCard card) {
			this.card = card;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final CMCard card;

	private CardEngine(final Builder builder) {
		this.card = builder.card;
	}

	@Override
	public Object eval(final String expression) {
		if (ID_ATTRIBUTE.equalsIgnoreCase(expression)) {
			return card.getId();
		}
		return new ForwardingAttributeTypeVisitor() {

			private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

			private Object adapted;

			@Override
			protected CMAttributeTypeVisitor delegate() {
				return DELEGATE;
			}

			public Object adapt(final Object value) {
				adapted = value;
				card.getType().getAttribute(expression).getType().accept(this);
				return adapted;
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				adapted = IdAndDescription.class.cast(adapted).getId();
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				adapted = IdAndDescription.class.cast(adapted).getId();
			};

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				adapted = IdAndDescription.class.cast(adapted).getId();
			}

		}.adapt(card.get(expression));
	}

}
