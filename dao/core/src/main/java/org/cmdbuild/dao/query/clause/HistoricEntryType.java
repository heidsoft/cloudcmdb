package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMEntryType;

public interface HistoricEntryType<T extends CMEntryType> {

	T getType();

}
