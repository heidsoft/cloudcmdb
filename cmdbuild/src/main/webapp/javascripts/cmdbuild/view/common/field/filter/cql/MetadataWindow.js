(function () {

	Ext.define('CMDBuild.view.common.field.filter.cql.MetadataWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.filter.cql.Metadata'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.cql.Metadata}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		closeAction: 'hide',
		title: CMDBuild.Translation.editMetadata,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								scope: this,

								handler: function (button, e) {
									this.grid.getStore().insert(0, Ext.create('CMDBuild.model.common.field.filter.cql.Metadata'));
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

								handler: function (button, e) {
									this.delegate.cmfg('onMetadataWindowSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onMetadataWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
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

										handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
											grid.getStore().remove(record);
										}
									})
								]
							})
						],

						store: Ext.create('Ext.data.Store', {
							model: 'CMDBuild.model.common.field.filter.cql.Metadata',
							data: []
						}),

						plugins: [
							Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (window, eOpts) {
				this.delegate.cmfg('onMetadataWindowShow');
			}
		}
	});

})();
