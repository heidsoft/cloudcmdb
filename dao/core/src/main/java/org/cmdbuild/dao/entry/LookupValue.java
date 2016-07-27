package org.cmdbuild.dao.entry;

public class LookupValue extends IdAndDescription {

	private final String lookupType;
	private final String translationUuid;

	public LookupValue( //
			final Long id, //
			final String description, //
			final String lookupType, //
			final String translationUuid) {
		super(id, description);
		this.lookupType = lookupType;
		this.translationUuid = translationUuid;
	}

	public String getLooupType() {
		return lookupType;
	}

	public String getTranslationUuid() {
		return translationUuid;
	}

}
