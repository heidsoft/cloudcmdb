(function () {

	Ext.define('CMDBuild.bim.proxy.Ifc', {

		requires: [
			'CMDBuild.bim.data.CMBimLayerModel',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		download: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.ifc.download });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		import: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.ifc.imports });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters, true);
		}
	});

})();
