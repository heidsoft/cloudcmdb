(function () {

	Ext.define('CMDBuild.proxy.email.Account', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.email.account.Store'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.email.account.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @params {Boolean} autoLoad
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function (autoLoad) {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.EMAIL, {
				autoLoad: autoLoad || false,
				model: 'CMDBuild.model.email.account.Store',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.email.account.readAll,
					reader: {
						type: 'json',
						root: 'response.elements'
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
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

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.email.account.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.email.account.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		setDefault: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.email.account.setDefault });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.email.account.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		}
	});

})();
