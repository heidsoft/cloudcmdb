(function () {

	Ext.define('CMDBuild.view.administration.widget.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.Widget'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,

		initComponent: function () {
			Ext.apply(this, {
				viewConfig: {
					plugins: {
						ptype: 'gridviewdragdrop',
						dragGroup: 'widgetsDDGroup',
						dropGroup: 'widgetsDDGroup'
					},
					listeners: {
						scope: this,
						drop: function (node, data, overModel, dropPosition, eOpts) {
							this.delegate.cmfg('onClassTabWidgetItemDrop');
						}
					}
				},
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.TYPE,
						text: CMDBuild.Translation.type,
						sortable: false,
						flex: 1,

						renderer: function (value) {
							return this.delegate.cmfg('classTabWidgetTypeRenderer', value);
						}
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.LABEL,
						text: CMDBuild.Translation.buttonLabel,
						sortable: false,
						flex: 2
					},
					Ext.create('Ext.ux.grid.column.Active', {
						dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
						text: CMDBuild.Translation.active,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					})
				],
				store: CMDBuild.proxy.widget.Widget.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onClassTabWidgetItemDoubleClick');
			},

			select: function (row, record, index) {
				this.delegate.cmfg('onClassTabWidgetRowSelected');
			}
		}
	});

})();
