package org.cmdbuild.model.widget.customform;

import static org.cmdbuild.logger.Log.WORKFLOW;

import org.apache.commons.lang3.builder.Builder;
import org.slf4j.Logger;

interface ModelBuilder extends Builder<String> {

	Logger logger = WORKFLOW;

}