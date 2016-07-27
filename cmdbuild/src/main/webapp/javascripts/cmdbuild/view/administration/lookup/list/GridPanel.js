(function() {

	Ext.define('CMDBuild.view.administration.lookup.list.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.lookup.Lookup'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.List}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addLookup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLookupListAddButtonClick');
								}
							}),
							'->',
							Ext.create('CMDBuild.view.common.field.GridLocalSearch', { grid: this })
						]
					})
				],
				viewConfig: {
					plugins: [
						{
							ptype: 'gridviewdragdrop',
							dragGroup: 'dd',
							dropGroup: 'dd'
						}
					],
					listeners: {
						scope: this,
						drop: function(node, data, overModel, dropPosition, eOpts) {
							this.delegate.cmfg('onLookupListDrop');
						}
					}
				},
				columns: [
					{
						dataIndex: 'Number',
						hideable: false,
						hidden: true
					},
					{
						dataIndex: 'Code',
						text: CMDBuild.Translation.code,
						flex: 1
					},
					{
						dataIndex: 'Description',
						text: CMDBuild.Translation.descriptionLabel,
						flex: 2
					},
					{
						dataIndex: 'ParentDescription',
						text: CMDBuild.Translation.parentDescription,
						flex: 2
					},
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'Active',
						text: CMDBuild.Translation.active,
						width: 60,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
						processEvent: Ext.emptyFn // Makes column readOnly
					})
				],
				store: CMDBuild.proxy.lookup.Lookup.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onLookupListItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onLookupListRowSelected');
			}
		}
	});

})();