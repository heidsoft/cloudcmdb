package org.cmdbuild.config;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

import org.cmdbuild.services.Settings;

public class GraphProperties extends DefaultProperties implements GraphConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "graph";

	private static final String BASE_LEVEL = "baseLevel";
	private static final String CLUSTERING_THRESHOLD = "clusteringThreshold";
	private static final String DISPLAY_LABEL = "displayLabel";
	private static final String EDGE_COLOR = "edgeColor";
	private static final String ENABLE_EDGE_TOOLTIP = "enableEdgeTooltip";
	private static final String ENABLE_NODE_TOOLTIP = "enableNodeTooltip";
	private static final String ENABLED = "enabled";
	private static final String SPRITE_DIMENSION = "spriteDimension";
	private static final String STEP_RADIUS = "stepRadius";
	private static final String VIEW_POINT_DISTANCE = "viewPointDistance";
	private static final String VIEW_POINT_HEIGHT = "viewPointHeight";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	public GraphProperties() {
		super();
		setProperty(BASE_LEVEL, "1");
		setProperty(CLUSTERING_THRESHOLD, "100");
		setProperty(DISPLAY_LABEL, "none");
		setProperty(EDGE_COLOR, "#3D85C6");
		setProperty(ENABLE_EDGE_TOOLTIP, TRUE.toString());
		setProperty(ENABLE_NODE_TOOLTIP, TRUE.toString());
		setProperty(ENABLED, TRUE.toString());
		setProperty(SPRITE_DIMENSION, "20");
		setProperty(STEP_RADIUS, "60");
		setProperty(VIEW_POINT_DISTANCE, "50");
		setProperty(VIEW_POINT_HEIGHT, "50");
		setProperty(USERNAME, "");
		setProperty(PASSWORD, "");
	}

	public static GraphProperties getInstance() {
		return (GraphProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		return parseBoolean(getProperty(ENABLED));
	}

	@Override
	public int getBaseLevel() {
		return parseInt(getProperty(BASE_LEVEL));
	}

	@Override
	public int getClusteringThreshold() {
		return parseInt(getProperty(CLUSTERING_THRESHOLD));
	}

	@Override
	public String getDisplayLabel() {
		return getProperty(DISPLAY_LABEL);
	}

	@Override
	public String getEdgeColor() {
		return getProperty(EDGE_COLOR);
	}

	@Override
	public boolean isEdgeTooltipEnabled() {
		return parseBoolean(getProperty(ENABLE_EDGE_TOOLTIP));
	}

	@Override
	public boolean isNodeTooltipEnabled() {
		return parseBoolean(getProperty(ENABLE_NODE_TOOLTIP));
	}

	@Override
	public int getSpriteDimension() {
		return parseInt(getProperty(SPRITE_DIMENSION));
	}

	@Override
	public int getStepRadius() {
		return parseInt(getProperty(STEP_RADIUS));
	}

	@Override
	public int getViewPointDistance() {
		return parseInt(getProperty(VIEW_POINT_DISTANCE));
	}

	@Override
	public int getViewPointHeight() {
		return parseInt(getProperty(VIEW_POINT_HEIGHT));
	}

}
