(function () {

	Ext.define('CMDBuild.proxy.classes.tabs.History', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.classes.tabs.history.CardRecord'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.HISTORY, {
				autoLoad: false,
				model: 'CMDBuild.model.classes.tabs.history.CardRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.history.classes.card.read,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE + '.' + CMDBuild.core.constants.Proxy.ELEMENTS
					}
				},
				sorters: [ // Setup sorters, also if server returns ordered collection
					{ property: CMDBuild.core.constants.Proxy.BEGIN_DATE, direction: 'DESC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAttributes: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attribute.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTRIBUTE, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readHistoric: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.history.classes.card.readHistoric });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readHistoricRelation: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.history.classes.card.readHistoricRelation });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readRelations: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.history.classes.card.readRelations });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.HISTORY, parameters);
		}
	});

})();
