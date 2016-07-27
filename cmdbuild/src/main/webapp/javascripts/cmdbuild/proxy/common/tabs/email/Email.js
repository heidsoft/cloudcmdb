(function () {

	Ext.define('CMDBuild.proxy.common.tabs.email.Email', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.common.tabs.email.Email'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.proxy.index.Json.email.create
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.EMAIL, {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.email.Email',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.email.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.STATUS, direction: 'ASC' }
				],
				groupField: CMDBuild.core.constants.Proxy.STATUS
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		isEmailEnabledForCard: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.proxy.index.Json.email.enabled
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.proxy.index.Json.email.remove
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.proxy.index.Json.email.update
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		}
	});

})();
