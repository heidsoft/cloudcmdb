(function() {

	Ext.define('CMDBuild.core.configurations.CustomPage', {

		singleton: true,

		config: {
			customizationsPath: 'upload/custompages/',
			version: '1.1.0' // GuiFramework version (used to build folder name)
		},

		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();