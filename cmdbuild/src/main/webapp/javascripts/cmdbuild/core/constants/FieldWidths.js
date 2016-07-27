(function() {

	var label = 150;
	var labelConfigure = 250;
	var labelConfiguration = 300;
	var standardBigFieldOnly = 475;
	var standardMediumFieldOnly = 150;
	var standardSmallFieldOnly = 100;

	Ext.define('CMDBuild.core.constants.FieldWidths', {

		singleton: true,

		// Labels
		LABEL: label,
		LABEL_LOGIN: 100,
		LABEL_CONFIGURE: labelConfigure,
		LABEL_CONFIGURATION: labelConfiguration,

		// Standard (base)
		STANDARD_BIG_FIELD_ONLY: standardBigFieldOnly,
		STANDARD_MEDIUM_FIELD_ONLY: standardMediumFieldOnly,
		STANDARD_SMALL_FIELD_ONLY: standardSmallFieldOnly,

		// Custom
		EDITOR_HTML: label + 600,
		MENU_DROPDOWN: 600,

		// Administration
		ADMINISTRATION_BIG: label + 250,
		ADMINISTRATION_MEDIUM: label + 150,
		ADMINISTRATION_SMALL: label + 80,

		// Configuration
		CONFIGURATION_BIG: labelConfiguration + 450,
		CONFIGURATION_MEDIUM: labelConfiguration + 150,
		CONFIGURATION_SMALL: labelConfiguration + 100,

		// Configure
		CONFIGURE_BIG: labelConfigure + 450,
		CONFIGURE_MEDIUM: labelConfigure + 150,
		CONFIGURE_SMALL: labelConfigure + 100,

		// Standard
		STANDARD_BIG: label + standardBigFieldOnly,
		STANDARD_MEDIUM: label + standardMediumFieldOnly,
		STANDARD_SMALL: label + standardSmallFieldOnly
	});

})();
