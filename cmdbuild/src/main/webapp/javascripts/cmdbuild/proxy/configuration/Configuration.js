(function () {

	Ext.define('CMDBuild.proxy.configuration.Configuration', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				params:{ names: Ext.JSON.encode(['bim', 'cmdbuild', 'dms', 'gis', 'graph', 'workflow']) },
				url: CMDBuild.proxy.index.Json.configuration.readAll
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		}
	});

})();
