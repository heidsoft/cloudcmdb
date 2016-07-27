(function () {

	Ext.define('CMDBuild.proxy.lookup.Type', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.lookup.Type'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.type.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.LOOKUP, {
				autoLoad: true,
				model: 'CMDBuild.model.lookup.Type',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.lookup.type.readAll,
					reader: {
						type: 'json'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.type.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.type.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.type.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		}
	});

})();
