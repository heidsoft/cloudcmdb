package org.cmdbuild.service.rest.v2.cxf;

import static org.cmdbuild.service.rest.v2.model.Models.newGraphConfiguration;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import org.cmdbuild.service.rest.v2.GraphConfiguration;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

public class CxfGraphConfiguration implements GraphConfiguration {

	private final org.cmdbuild.config.GraphConfiguration delegate;

	public CxfGraphConfiguration(final org.cmdbuild.config.GraphConfiguration delegate) {
		this.delegate = delegate;
	}

	@Override
	public ResponseSingle<org.cmdbuild.service.rest.v2.model.GraphConfiguration> read() {
		return newResponseSingle(org.cmdbuild.service.rest.v2.model.GraphConfiguration.class) //
				.withElement(newGraphConfiguration() //
						.withEnabledStatus(delegate.isEnabled()) //
						.withBaseLevel(delegate.getBaseLevel()) //
						.withClusteringThreshold(delegate.getClusteringThreshold()) //
						.withDisplayLabel(delegate.getDisplayLabel()) //
						.withEdgeColor(delegate.getEdgeColor()) //
						.withEdgeTooltipEnabled(delegate.isEdgeTooltipEnabled()) //
						.withNodeTooltipEnabled(delegate.isNodeTooltipEnabled()) //
						.withSpriteDimension(delegate.getSpriteDimension()) //
						.withStepRadius(delegate.getStepRadius()) //
						.withViewPointDistance(delegate.getViewPointDistance()) //
						.withViewPointHeight(delegate.getViewPointHeight()) //
						.build()) //
				.build();
	}

}
