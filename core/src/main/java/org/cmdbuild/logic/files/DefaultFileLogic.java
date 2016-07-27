package org.cmdbuild.logic.files;

import java.util.Map;

public class DefaultFileLogic implements FileLogic {

	private final Map<String, FileStore> map;

	public DefaultFileLogic(final Map<String, FileStore> map) {
		this.map = map;
	}

	@Override
	public FileStore fileStore(final String value) {
		return map.get(value);
	}

}
