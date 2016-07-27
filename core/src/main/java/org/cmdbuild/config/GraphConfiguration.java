package org.cmdbuild.config;

public interface GraphConfiguration {

	boolean isEnabled();

	int getBaseLevel();

	int getClusteringThreshold();

	String getDisplayLabel();

	String getEdgeColor();

	boolean isEdgeTooltipEnabled();

	boolean isNodeTooltipEnabled();

	int getSpriteDimension();

	int getStepRadius();

	int getViewPointDistance();

	int getViewPointHeight();

}
