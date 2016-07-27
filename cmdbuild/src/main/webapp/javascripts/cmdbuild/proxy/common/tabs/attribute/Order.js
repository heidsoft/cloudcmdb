(function () {

	Ext.define('CMDBuild.proxy.common.tabs.attribute.Order', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.common.tabs.attribute.OrderGridRecord'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.ATTRIBUTE, {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.attribute.OrderGridRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.attribute.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ATTRIBUTES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreOrderSign: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[0, CMDBuild.Translation.administration.modClass.attributeProperties.not_in_use],
					[1, CMDBuild.Translation.administration.modClass.attributeProperties.direction.asc],
					[-1, CMDBuild.Translation.administration.modClass.attributeProperties.direction.desc]
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		reorder: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attribute.sorting.reorder });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTRIBUTE, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attribute.sorting.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTRIBUTE, parameters, true);
		}
	});

})();
