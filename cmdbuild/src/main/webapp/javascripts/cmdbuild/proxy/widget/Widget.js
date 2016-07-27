(function () {

	Ext.define('CMDBuild.proxy.widget.Widget', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.widget.DefinitionGrid'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.widget.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.WIDGET, {
				autoLoad: false,
				model: 'CMDBuild.model.widget.DefinitionGrid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.widget.readAllForClass,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
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

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.widget.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.widget.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.widget.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 *
		 * TODO: future implementation
		 */
		setSorting: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.widget.setSorting });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.widget.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		}
	});

})();
