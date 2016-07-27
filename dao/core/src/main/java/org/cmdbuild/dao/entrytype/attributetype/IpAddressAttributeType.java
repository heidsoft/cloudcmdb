package org.cmdbuild.dao.entrytype.attributetype;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class IpAddressAttributeType extends AbstractAttributeType<String> {

	public static enum Type {
		IPV4, //
		IPV6, //
		;

		/**
		 * Returns the enum constant with the specified name (case-insensitive).
		 * 
		 * @throws IllegalArgumentException
		 *             if no enum corresponds with the specified name
		 */
		public static Type of(final String name) {
			return of(name, null);
		}

		/**
		 * Returns the enum constant with the specified name (case-insensitive).
		 * 
		 * @param name
		 * @param defaultValue
		 * 
		 * @throws IllegalArgumentException
		 *             if no enum corresponds with the specified name and no
		 *             default value has been specified.
		 */
		public static Type of(final String name, final Type defaultValue) {
			for (final Type value : values()) {
				if (value.name().toLowerCase().equals(name)) {
					return value;
				}
			}
			if (defaultValue == null) {
				throw new NoSuchElementException(name);
			}
			return defaultValue;
		}

	}

	private static final String IPV4SEG = "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])";
	private static final String IPV4ADDR = "(" + IPV4SEG + "\\.){3,3}" + IPV4SEG;
	private static final String IPV6SEG = "[0-9a-fA-F]{1,4}";
	private static final String IPV6ADDR = "(" //
			+ "(" + IPV6SEG + ":){7,7}" + IPV6SEG + "|" //
			+ "(" + IPV6SEG + ":){1,7}:|" //
			+ "(" + IPV6SEG + ":){1,6}:" + IPV6SEG + "|" //
			+ "(" + IPV6SEG + ":){1,5}(:" + IPV6SEG + "){1,2}|" //
			+ "(" + IPV6SEG + ":){1,4}(:" + IPV6SEG + "){1,3}|" //
			+ "(" + IPV6SEG + ":){1,3}(:" + IPV6SEG + "){1,4}|" //
			+ "(" + IPV6SEG + ":){1,2}(:" + IPV6SEG + "){1,5}|" //
			+ IPV6SEG + ":((:" + IPV6SEG + "){1,6})|" //
			+ ":((:" + IPV6SEG + "){1,7}|:)|" //
			+ "fe80:(:" + IPV6SEG + "){0,4}%[0-9a-zA-Z]{1,}|" //
			+ "::(ffff(:0{1,4}){0,1}:){0,1}" + IPV4ADDR + "|" //
			+ "(" + IPV6SEG + ":){1,4}:" + IPV4ADDR //
			+ ")";
	private static final String CLASS_SEPARATOR_REGEX = "/";
	private static final String IPV4_CLASS_REGEX = "(3[0-2]|[1-2][0-9]|[8-9])";
	private static final Pattern IPV4_PATTERN = Pattern.compile(EMPTY //
			+ "^" //
			+ IPV4ADDR + "(" + CLASS_SEPARATOR_REGEX + IPV4_CLASS_REGEX + ")*"//
			+ "$" //
	);
	private static final String IPV6_CLASS_REGEX = "([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8])";
	private static final Pattern IPV6_PATTERN = Pattern.compile(EMPTY //
			+ "^" //
			+ IPV6ADDR + "(" + CLASS_SEPARATOR_REGEX + IPV6_CLASS_REGEX + ")*"//
			+ "$" //
	);

	private final Type type;

	public IpAddressAttributeType(final Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected String convertNotNullValue(final Object value) {
		final String stringValue = defaultIfNull(value, EMPTY).toString().trim();
		if (isEmpty(stringValue)) {
			return null;
		} else if ((type == Type.IPV4) && IPV4_PATTERN.matcher(stringValue).matches()) {
			return stringValue;
		} else if ((type == Type.IPV6) && IPV6_PATTERN.matcher(stringValue).matches()) {
			return stringValue;
		} else {
			throw illegalValue(value);
		}
	}

}
