(function () {

	Ext.define('CMDBuild.view.administration.class.CMAttributeSortingGrid', {
		extend: 'Ext.grid.Panel',


		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.tabs.attribute.Order'
		],

		border: false,
		filter: false,
		filtering: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER,
						id: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER,
						menuDisabled: true,
						hidden: true
					}),
					Ext.create('Ext.grid.column.Column', {
						id: CMDBuild.core.constants.Proxy.NAME,
						text: CMDBuild.Translation.administration.modClass.attributeProperties.name,
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						flex: 1
					}),
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.administration.modClass.attributeProperties.description,
						id: CMDBuild.core.constants.Proxy.DESCRIPTION,
						flex: 1
					}),
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.CLASS_ORDER_SIGN,
						text: CMDBuild.Translation.administration.modClass.attributeProperties.criterion,
						flex: 1,

						editor: {
							xtype: 'combo',
							valueField: CMDBuild.core.constants.Proxy.VALUE,
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							forceSelection: true,
							editable: false,

							store: CMDBuild.proxy.common.tabs.attribute.Order.getStoreOrderSign(),
							queryMode: 'local'
						},

						renderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
							if (value > 0) {
								return '<span>' + CMDBuild.Translation.administration.modClass.attributeProperties.direction.asc + '</span>';
							} else if (value < 0) {
								return '<span>' + CMDBuild.Translation.administration.modClass.attributeProperties.direction.desc + '</span>';
							} else {
								return '<span>' + CMDBuild.Translation.administration.modClass.attributeProperties.not_in_use + '</span>';
							}
						}
					})
				],
				store: CMDBuild.proxy.common.tabs.attribute.Order.getStore(),
				plugins: [
					Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
				],
				viewConfig: {
					plugins: {
						ptype: 'gridviewdragdrop',
						dragGroup: 'dd',
						dropGroup: 'dd'
					}
				}
			});

			this.callParent(arguments);

			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.idClass);

			this.getStore().load({ params: params });
		}
	});

})();
