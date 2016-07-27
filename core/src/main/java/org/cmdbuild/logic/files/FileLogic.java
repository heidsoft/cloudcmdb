package org.cmdbuild.logic.files;

import org.cmdbuild.logic.Logic;

public interface FileLogic extends Logic {

	FileStore fileStore(String value);

}
