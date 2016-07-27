package org.cmdbuild.services.sync.store;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.sync.store.Predicates.keyAttributes;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Maps;

public class ClassType extends AbstractType {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassType> {

		private String name;
		private final Map<String, Attribute> attributes = Maps.newHashMap();

		private Builder() {
			// use factory method
		}

		@Override
		public ClassType build() {
			validate();
			return new ClassType(this);
		}

		private void validate() {
			Validate.notBlank(name, "invalid name");
			Validate.isTrue(!from(attributes.values()) //
					.filter(keyAttributes()) //
					.isEmpty(), "missing key attribute(s)");
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withAttribute(final Attribute attribute) {
			if (attribute != null) {
				this.attributes.put(attribute.getName(), attribute);
			}
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String name;
	private final Map<String, Attribute> attributes;

	private ClassType(final Builder builder) {
		this.name = builder.name;
		this.attributes = builder.attributes;
	}

	@Override
	public void accept(final TypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ClassType)) {
			return false;
		}
		final ClassType other = ClassType.class.cast(obj);
		return name.equals(other.name);
	}

	@Override
	protected int doHashCode() {
		return name.hashCode();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterable<Attribute> getAttributes() {
		return attributes.values();
	}

	@Override
	public Attribute getAttribute(final String name) {
		return attributes.get(name);
	}

	@Override
	protected String doToString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("name", name) //
				.append("attributes", attributes) //
				.toString();
	}

}
