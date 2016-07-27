package org.cmdbuild.services.store.menu;

import java.util.Map;

import org.cmdbuild.services.localization.LocalizableStorableVisitor;

public class MenuElementImpl implements MenuElement {

	private final Long id;
	private final String description;
	private final MenuItemType type;
	private final Integer parentId;
	private final Integer number;
	private final String elementClassName;
	private final Integer elementId;
	private final String groupName;
	private final String uuid;
	private final Map<String, Object> specificTypeValues;

	public MenuElementImpl(final MenuElementBuilder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.elementId = builder.elementId;
		this.elementClassName = builder.elementClassName;
		this.groupName = builder.groupName;
		this.number = builder.number;
		this.parentId = builder.parentId;
		this.specificTypeValues = builder.specificTypeValues;
		this.type = builder.type;
		this.uuid = builder.uuid;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getIdentifier() {
		return String.valueOf(id);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getCode() {
		return uuid;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public MenuItemType getType() {
		return type;
	}

	@Override
	public Integer getParentId() {
		return parentId;
	}

	@Override
	public Integer getNumber() {
		return number;
	}

	@Override
	public String getElementClassName() {
		return elementClassName;
	}

	@Override
	public Integer getElementId() {
		return elementId;
	}

	@Override
	public String getGroupName() {
		return groupName;
	}

	@Override
	public Map<String, Object> getSpecificTypeValues() {
		return specificTypeValues;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	public static class MenuElementBuilder implements org.apache.commons.lang3.builder.Builder<MenuElementImpl> {

		private Long id;
		private String description;
		private Integer elementId;
		private String elementClassName;
		private String groupName;
		private Integer number;
		private Integer parentId;
		private MenuItemType type;
		private String uuid;
		private Map<String, Object> specificTypeValues;

		@Override
		public MenuElementImpl build() {
			return new MenuElementImpl(this);
		}

		public MenuElementBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public MenuElementBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public MenuElementBuilder withType(final MenuItemType type) {
			this.type = type;
			return this;
		}

		public MenuElementBuilder withParentId(final Integer parentId) {
			this.parentId = parentId;
			return this;
		}

		public MenuElementBuilder withNumber(final Integer number) {
			this.number = number;
			return this;
		}

		public MenuElementBuilder withElementId(final Integer elementId) {
			this.elementId = elementId;
			return this;
		}

		public MenuElementBuilder withGroupName(final String groupName) {
			this.groupName = groupName;
			return this;
		}

		public MenuElementBuilder withUuid(final String uuid) {
			this.uuid = uuid;
			return this;
		}

		public MenuElementBuilder withElementClassName(final String className) {
			this.elementClassName = className;
			return this;
		}

		public MenuElementBuilder withSpecificTypeValues(final Map<String, Object> specificTypeValues) {
			this.specificTypeValues = specificTypeValues;
			return this;
		}

	}

	public static MenuElementBuilder newInstance() {
		return new MenuElementBuilder();
	}

}
