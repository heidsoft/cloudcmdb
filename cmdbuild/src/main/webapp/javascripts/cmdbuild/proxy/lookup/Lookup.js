(function () {

	Ext.define('CMDBuild.proxy.lookup.Lookup', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.lookup.GridStore',
			'CMDBuild.model.lookup.Parent'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		disable: function (parameters, disable) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.disable });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		enable: function (parameters, disable) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.enable });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.LOOKUP, {
				autoLoad: false,
				model: 'CMDBuild.model.lookup.GridStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.lookup.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
				},
				sorters: [
					{ property: 'Number', direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreParents: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.LOOKUP, {
				autoLoad: false,
				model: 'CMDBuild.model.lookup.Parent',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.lookup.readAllParents,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					}
				},
				sorters: [
					{ property: 'ParentDescription', direction: 'ASC' }
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

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		setOrder: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.setOrder });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.lookup.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		}
	});

})();
