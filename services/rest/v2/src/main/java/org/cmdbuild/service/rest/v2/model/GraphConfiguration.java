package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.BASE_LEVEL;
import static org.cmdbuild.service.rest.v2.constants.Serialization.CLUSTERING_THRESHOLD;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DISPLAY_LABEL;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EDGE_COLOR;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EDGE_TOOLTIP_ENABLED;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ENABLED;
import static org.cmdbuild.service.rest.v2.constants.Serialization.NODE_TOOLTIP_ENABLED;
import static org.cmdbuild.service.rest.v2.constants.Serialization.SPRITE_DIMENSION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.STEP_RADIUS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.VIEW_POINT_DISTANCE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.VIEW_POINT_HEIGHT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class GraphConfiguration extends AbstractModel {

	private boolean enabled;
	private int baseLevel;
	private int clusteringThreshold;
	private String displayLabel;
	private String edgeColor;
	private boolean edgeTooltipEnabled;
	private boolean nodeTooltipEnabled;
	private int spriteDimension;
	private int stepRadius;
	private int viewPointDistance;
	private int viewPointHeight;

	GraphConfiguration() {
		// package visibility
	}

	@XmlAttribute(name = ENABLED)
	public boolean isEnabled() {
		return enabled;
	}

	void setEnabled(final boolean value) {
		this.enabled = value;
	}

	@XmlAttribute(name = BASE_LEVEL)
	public int getBaseLevel() {
		return baseLevel;
	}

	void setBaseLevel(final int value) {
		this.baseLevel = value;
	}

	@XmlAttribute(name = CLUSTERING_THRESHOLD)
	public int getClusteringThreshold() {
		return clusteringThreshold;
	}

	void setClusteringThreshold(final int value) {
		this.clusteringThreshold = value;
	}

	@XmlAttribute(name = DISPLAY_LABEL)
	public String getDisplayLabel() {
		return displayLabel;
	}

	void setDisplayLabel(final String value) {
		this.displayLabel = value;
	}

	@XmlAttribute(name = EDGE_COLOR)
	public String getEdgeColor() {
		return edgeColor;
	}

	void setEdgeColor(final String value) {
		this.edgeColor = value;
	}

	@XmlAttribute(name = EDGE_TOOLTIP_ENABLED)
	public boolean isEdgeTooltipEnabled() {
		return edgeTooltipEnabled;
	}

	void setEdgeTooltipEnabled(final boolean value) {
		this.edgeTooltipEnabled = value;
	}

	@XmlAttribute(name = NODE_TOOLTIP_ENABLED)
	public boolean isNodeTooltipEnabled() {
		return nodeTooltipEnabled;
	}

	void setNodeTooltipEnabled(final boolean value) {
		this.nodeTooltipEnabled = value;
	}

	@XmlAttribute(name = SPRITE_DIMENSION)
	public int getSpriteDimension() {
		return spriteDimension;
	}

	void setSpriteDimension(final int value) {
		this.spriteDimension = value;
	}

	@XmlAttribute(name = STEP_RADIUS)
	public int getStepRadius() {
		return stepRadius;
	}

	void setStepRadius(final int stepRadius) {
		this.stepRadius = stepRadius;
	}

	@XmlAttribute(name = VIEW_POINT_DISTANCE)
	public int getViewPointDistance() {
		return viewPointDistance;
	}

	void setViewPointDistance(final int value) {
		this.viewPointDistance = value;
	}

	@XmlAttribute(name = VIEW_POINT_HEIGHT)
	public int getViewPointHeight() {
		return viewPointHeight;
	}

	public void setViewPointHeight(final int value) {
		this.viewPointHeight = value;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof LongId)) {
			return false;
		}

		final GraphConfiguration other = GraphConfiguration.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.enabled, other.enabled) //
				.append(this.baseLevel, other.baseLevel) //
				.append(this.clusteringThreshold, other.clusteringThreshold) //
				.append(this.displayLabel, other.displayLabel) //
				.append(this.edgeColor, other.edgeColor) //
				.append(this.edgeTooltipEnabled, other.edgeTooltipEnabled) //
				.append(this.nodeTooltipEnabled, other.nodeTooltipEnabled) //
				.append(this.spriteDimension, other.spriteDimension) //
				.append(this.stepRadius, other.stepRadius) //
				.append(this.viewPointDistance, other.viewPointDistance) //
				.append(this.viewPointHeight, other.viewPointHeight) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.enabled) //
				.append(this.baseLevel) //
				.append(this.clusteringThreshold) //
				.append(this.displayLabel) //
				.append(this.edgeColor) //
				.append(this.edgeTooltipEnabled) //
				.append(this.nodeTooltipEnabled) //
				.append(this.spriteDimension) //
				.append(this.stepRadius) //
				.append(this.viewPointDistance) //
				.append(this.viewPointHeight) //
				.toHashCode();
	}

}
