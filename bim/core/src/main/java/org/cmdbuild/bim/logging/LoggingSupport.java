package org.cmdbuild.bim.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public interface LoggingSupport {

	String LOGGER_NAME = "bim";

	Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

	Marker geometry = MarkerFactory.getMarker("geometry");

}
