package org.cmdbuild.service.rest.v1.cxf.serialization;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.Constants.REST_ALL_DATES_PATTERN;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.joda.time.DateTime;

public class DefaultConverter implements Converter {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DefaultConverter> {

		@Override
		public DefaultConverter build() {
			return new DefaultConverter(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final DateFormat dateFormat = new SimpleDateFormat(REST_ALL_DATES_PATTERN);

	private static class ToClientImpl extends ForwardingAttributeTypeVisitor implements ValueConverter {

		private static final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

		private Object input;
		private Object output;

		@Override
		protected CMAttributeTypeVisitor delegate() {
			return DELEGATE;
		}

		@Override
		public Object convert(final CMAttributeType<?> attributeType, final Object value) {
			output = input = value;
			attributeType.accept(this);
			return output;
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			output = convert(attributeType.convertValue(input));
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			output = convert(attributeType.convertValue(input));
		}

		@Override
		public void visit(final ForeignKeyAttributeType attributeType) {
			output = convert(attributeType.convertValue(input));
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			output = convert(attributeType.convertValue(input));
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			output = convert(attributeType.convertValue(input));
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			output = convert(attributeType.convertValue(input));
		}

		private static Object convert(final DateTime value) {
			return (value == null) ? null : dateFormat.format(value.toDate());
		}

		private static Object convert(final IdAndDescription value) {
			return (value == null) ? null : value.getId();
		}

	}

	private static class FromClientImpl extends ForwardingAttributeTypeVisitor implements ValueConverter {

		private static final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

		private Object input;
		private Object output;

		@Override
		protected CMAttributeTypeVisitor delegate() {
			return DELEGATE;
		}

		@Override
		public Object convert(final CMAttributeType<?> attributeType, final Object value) {
			output = input = value;
			attributeType.accept(this);
			return output;
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			output = convertDate(input);
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			output = convertDate(input);
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			output = convertDate(input);
		}

		private static Object convertDate(final Object input) {
			try {
				final String s = String.class.cast(input);
				return isBlank(s) ? null : dateFormat.parse(s);
			} catch (final ParseException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private DefaultConverter(final Builder builder) {
		// nothing to do
	}

	@Override
	public ValueConverter toClient() {
		return new ToClientImpl();
	}

	@Override
	public ValueConverter fromClient() {
		return new FromClientImpl();
	}

}
