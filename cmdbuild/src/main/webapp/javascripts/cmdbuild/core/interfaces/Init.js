(function () {

	Ext.define('CMDBuild.core.interfaces.Init', {

		/**
		 * Server interfaces initialization
		 *
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configurations

			// Global interfaces configuration object
			Ext.ns('CMDBuild.global.interfaces.Configurations');
			CMDBuild.global.interfaces.Configurations = Ext.create('CMDBuild.core.interfaces.Configurations');
		}
	});

})();
