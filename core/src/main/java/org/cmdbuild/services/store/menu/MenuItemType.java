package org.cmdbuild.services.store.menu;

public enum MenuItemType {

	CLASS("class"), //
	DASHBOARD("dashboard"), //
	PROCESS("processclass"), //
	FOLDER("folder"), //
	SYSTEM_FOLDER("system_folder"), //
	REPORT_CSV("reportcsv"), //
	REPORT_PDF("reportpdf"), //
	REPORT_ODT("reportodt"), //
	REPORT_XML("reportxml"), //
	VIEW("view"), //
	CUSTOM_PAGE("custompage"), //
	ROOT("root"), //
	;

	private final String value;

	private MenuItemType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static MenuItemType getType(final String type) {
		for (final MenuItemType itemType : values()) {
			if (itemType.getValue().equals(type)) {
				return itemType;
			}
		}
		return null;
	}

	public static boolean isReport(final MenuItemType type) {
		return type.equals(REPORT_CSV) || //
				type.equals(REPORT_ODT) || //
				type.equals(REPORT_PDF) || //
				type.equals(REPORT_XML);
	}

	public static boolean isClassOrProcess(final MenuItemType type) {
		return type.equals(CLASS) || type.equals(PROCESS);
	}

	public static boolean isDashboard(final MenuItemType type) {
		return type.equals(DASHBOARD);
	}

	public static boolean isView(final MenuItemType type) {
		return type.equals(VIEW);
	}

	public static boolean isCustomPage(final MenuItemType type) {
		return type.equals(CUSTOM_PAGE);
	}

}