(function() {

	Ext.define('CMDBuild.view.administration.email.template.ValuesWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.email.template.Variable'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.template.Template}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		autoScroll: true,
		title: CMDBuild.Translation.editValues,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								scope: this,

								handler: function(buttons, e) {
									this.grid.getStore().insert(0, Ext.create('CMDBuild.model.email.template.Variable'));
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
									this.delegate.cmfg('onEmailTemplateValuesWindowSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplateValuesWindowAbortButtonClick');
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
								dataIndex: CMDBuild.core.constants.Proxy.KEY,
								text: CMDBuild.Translation.key,
								flex: 1,

								editor: { xtype: 'textfield' }
							},
							{
								dataIndex: CMDBuild.core.constants.Proxy.VALUE,
								text: CMDBuild.Translation.value,
								flex: 1,

								editor: { xtype: 'textfield' }
							},
							Ext.create('Ext.grid.column.Action', {
								align: 'center',
								width: 30,
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true,

								items: [
									Ext.create('CMDBuild.core.buttons.iconized.Remove', {
										withSpacer: true,
										tooltip: CMDBuild.Translation.remove,
										scope: this,

										handler: function(view, rowIndex, colIndex, item, e, record) {
											this.delegate.cmfg('onEmailTemplateValuesWindowDeleteRowButtonClick', record);

										}
									})
								]
							})
						],

						store: Ext.create('Ext.data.Store', {
							model: 'CMDBuild.model.email.template.Variable',
							data: []
						}),

						plugins: [
							Ext.create('Ext.grid.plugin.CellEditing', {
								clicksToEdit: 1
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();