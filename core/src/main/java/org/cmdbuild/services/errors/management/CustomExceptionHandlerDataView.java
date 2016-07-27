package org.cmdbuild.services.errors.management;

import static com.google.common.base.Throwables.getRootCause;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.ForwardingCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;
import org.cmdbuild.exception.ORMException;

public class CustomExceptionHandlerDataView extends ForwardingDataView {

	private static final String PREFIX = "CM_CUSTOM_EXCEPTION:";

	private final CMDataView delegate;

	public CustomExceptionHandlerDataView(final CMDataView delegate) {
		this.delegate = delegate;
	}

	@Override
	protected CMDataView delegate() {
		return delegate;
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		return proxy(super.createCardFor(type));
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return proxy(super.update(card));
	}

	@Override
	public void delete(final CMCard card) {
		try {
			super.delete(card);
		} catch (final RuntimeException e) {
			throw translate(e);
		}
	}

	private static CMCardDefinition proxy(final CMCardDefinition delegate) {
		return new ForwardingCardDefinition(delegate) {

			@Override
			public CMCard save() {
				try {
					return super.save();
				} catch (final RuntimeException e) {
					throw translate(e);
				}
			}

		};
	}

	private static RuntimeException translate(final RuntimeException e) {
		final Throwable root = getRootCause(e);
		final RuntimeException _e;
		final String message = root.getMessage();
		if (contains(message, PREFIX)) {
			final String _message = message //
					.substring(message.indexOf(PREFIX) + PREFIX.length()) //
					// first line only
					.split(LINE_SEPARATOR, 2)[0] //
					.trim();
			_e = ORMException.ORMExceptionType.ORM_CUSTOM_EXCEPTION.createException(_message);
		} else {
			_e = e;
		}
		return _e;
	}

}
