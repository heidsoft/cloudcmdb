(function() {

	Ext.define('CMDBuild.view.administration.common.attributes.metadata.MetadataWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.attributes.Metadata'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.common.attributes.Metadata}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		autoScroll: true,
		title: CMDBuild.Translation.editMetadata,

		initComponent: function() {
			this.grid = Ext.create('Ext.grid.Panel', {
				border: false,
				frame: false,

				columns: [
					{
						text: CMDBuild.Translation.key,
						dataIndex: CMDBuild.core.constants.Proxy.KEY,
						flex: 1,

						editor: { xtype: 'textfield' }
					},
					{
						text: CMDBuild.Translation.value,
						dataIndex: CMDBuild.core.constants.Proxy.VALUE,
						flex: 1,

						editor: { xtype: 'textfield' }
					},
					Ext.create('Ext.grid.column.Action', {
						align: 'center',
						width: 25,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								tooltip: CMDBuild.Translation.remove,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									grid.getStore().remove(record);
								}
							})
						]
					})
				],

				store: Ext.create('Ext.data.Store', { // TODO: use Ext.data.ArrayStore
					model: 'CMDBuild.model.common.attributes.Metadata',
					data: []
				}),

				plugins: [
					Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1
					})
				]
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								scope: this,

								handler: function(button, e) {
									this.grid.getStore().insert(0, Ext.create('CMDBuild.model.common.attributes.Metadata'));
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onMetadataWindowSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onMetadataWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.grid]
			});

			this.callParent(arguments);
		}
	});

})();