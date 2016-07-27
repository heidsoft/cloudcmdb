(function() {

	Ext.define('CMDBuild.core.configurations.DataFormat', {

		singleton: true,

		config: {
			date: 'd/m/Y',
			dateTime: 'd/m/Y H:i:s',
			time: 'H:i:s'
		},

		/**
		 * @param {Object} config
		 */
		constructor: function(config) {
			this.initConfig(config);
		}
	});

})();