package org.cmdbuild.model.view;

import org.cmdbuild.services.localization.LocalizableStorableVisitor;

public class ViewImpl implements View {

	private Long id;
	private String name;
	private String description;
	private String sourceClassName;
	private String sourceFunction;
	private String filter;
	private ViewType type;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String getSourceClassName() {
		return sourceClassName;
	}

	public void setSourceClassName(final String sourceClassName) {
		this.sourceClassName = sourceClassName;
	}

	@Override
	public String getSourceFunction() {
		return sourceFunction;
	}

	public void setSourceFunction(final String sourceFunction) {
		this.sourceFunction = sourceFunction;
	}

	@Override
	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	@Override
	public ViewType getType() {
		return type;
	}

	public void setType(final ViewType type) {
		this.type = type;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	@Override
	public String getPrivilegeId() {
		return String.format("View:%d", getId());
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}
}
