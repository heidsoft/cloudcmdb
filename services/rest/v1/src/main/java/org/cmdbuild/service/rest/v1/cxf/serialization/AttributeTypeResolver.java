package org.cmdbuild.service.rest.v1.cxf.serialization;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.service.rest.v1.model.AttributeType;

public class AttributeTypeResolver implements CMAttributeTypeVisitor {

	private AttributeType attributeType;

	public AttributeType resolve(final CMAttribute attribute) {
		return resolve(attribute.getType());
	}

	public AttributeType resolve(final CMAttributeType<?> attributeType) {
		attributeType.accept(this);
		return this.attributeType;
	}

	@Override
	public void visit(final BooleanAttributeType attributeType) {
		this.attributeType = AttributeType.BOOLEAN;
	}

	@Override
	public void visit(final CharAttributeType attributeType) {
		this.attributeType = AttributeType.CHAR;
	}

	@Override
	public void visit(final DateAttributeType attributeType) {
		this.attributeType = AttributeType.DATE;
	}

	@Override
	public void visit(final DateTimeAttributeType attributeType) {
		this.attributeType = AttributeType.DATE_TIME;
	}

	@Override
	public void visit(final DoubleAttributeType attributeType) {
		this.attributeType = AttributeType.DOUBLE;
	}

	@Override
	public void visit(final DecimalAttributeType attributeType) {
		this.attributeType = AttributeType.DECIMAL;
	}

	@Override
	public void visit(final EntryTypeAttributeType attributeType) {
		this.attributeType = AttributeType.ENTRY_TYPE;
	}

	@Override
	public void visit(final ForeignKeyAttributeType attributeType) {
		this.attributeType = AttributeType.FOREIGN_KEY;
	}

	@Override
	public void visit(final IntegerAttributeType attributeType) {
		this.attributeType = AttributeType.INTEGER;
	}

	@Override
	public void visit(final IpAddressAttributeType attributeType) {
		this.attributeType = AttributeType.IP_ADDRESS;
	}

	@Override
	public void visit(final LookupAttributeType attributeType) {
		this.attributeType = AttributeType.LOOKUP;
	}

	@Override
	public void visit(final ReferenceAttributeType attributeType) {
		this.attributeType = AttributeType.REFERENCE;
	}

	@Override
	public void visit(final StringAttributeType attributeType) {
		this.attributeType = AttributeType.STRING;
	}

	@Override
	public void visit(final StringArrayAttributeType attributeType) {
		this.attributeType = AttributeType.STRING_ARRAY;
	}

	@Override
	public void visit(final TextAttributeType attributeType) {
		this.attributeType = AttributeType.TEXT;
	}

	@Override
	public void visit(final TimeAttributeType attributeType) {
		this.attributeType = AttributeType.TIME;
	}

}
