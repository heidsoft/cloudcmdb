package org.cmdbuild.services.bim.connector;

import org.cmdbuild.auth.logging.LoggingSupport;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.slf4j.Logger;

public interface CardDiffer {
	
	Logger logger = LoggingSupport.logger;

	CMCard updateCard(Entity sourceEntity, CMCard oldCard);

	CMCard createCard(Entity sourceEntity);

}