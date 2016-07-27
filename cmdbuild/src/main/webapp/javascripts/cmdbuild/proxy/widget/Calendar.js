(function () {

	Ext.define('CMDBuild.proxy.widget.Calendar', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.widget.calendar.AttributeCombo',
			'CMDBuild.model.widget.calendar.TargetClass'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @administration
		 */
		getStoreTargetClass: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.widget.calendar.TargetClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						active: true,
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function (record) { // Filters root of all classes
						return record.get(CMDBuild.core.constants.Proxy.NAME) != 'Class';
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @administration
		 */
		getStoreAttributesDate: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.ATTRIBUTE, {
				autoLoad: false,
				model: 'CMDBuild.model.widget.calendar.AttributeCombo',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.attribute.read,
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
				filters: [
					function (record) { // Filters by attribute's type
						return Ext.Array.contains(['DATE', 'TIMESTAMP'], record.get(CMDBuild.core.constants.Proxy.TYPE));
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @administration
		 */
		getStoreAttributesString: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.ATTRIBUTE, {
				autoLoad: false,
				model: 'CMDBuild.model.widget.calendar.AttributeCombo',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.attribute.read,
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
				filters: [
					function (record) { // Filters by attribute's type
						return Ext.Array.contains(['TEXT', 'STRING'], record.get(CMDBuild.core.constants.Proxy.TYPE));
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();
