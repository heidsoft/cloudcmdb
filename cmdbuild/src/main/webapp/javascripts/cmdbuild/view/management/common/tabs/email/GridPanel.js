(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.tabs.email.Email'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		delegate: undefined,

		overflowY: 'auto',
		border: false,
		collapsible: false,
		frame: false,

		initComponent: function () {
			var me = this;

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							this.buttonAdd = Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.composeEmail,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTabEmailGridAddEmailButtonClick');
								},

								isDisabled: function () {
									return (
										this.delegate.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
										|| !this.delegate.cmfg('tabEmailEditModeGet')
									);
								}
							}),
							this.buttonRegenerate = Ext.create('CMDBuild.core.buttons.email.Regenerate', {
								text: CMDBuild.Translation.regenerateAllEmails,
								scope: this,

								handler: function (button, e) {
									Ext.Msg.show({ // Ask to the user if is sure to delete all the unsent e-mails before
										title: CMDBuild.Translation.common.confirmpopup.title,
										msg: CMDBuild.Translation.emailRegenerationConfirmPopupText,
										buttons: Ext.Msg.OKCANCEL,
										icon: Ext.Msg.WARNING,
										scope: this,

										fn: function (buttonId, text, opt) {
											if (buttonId == 'ok')
												this.delegate.cmfg('onTabEmailGlobalRegenerationButtonClick');
										}
									});
								},

								isDisabled: function () {
									return (
										this.delegate.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
										|| !this.delegate.cmfg('tabEmailEditModeGet')
									);
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Reload', {
								text: CMDBuild.Translation.gridRefresh,
								forceDisabledState: false, // Force enabled state
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('tabEmailGridStoreLoad');
								}
							})
						]
					})
				],
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.STATUS,
						hidden: true
					},
					{
						text: CMDBuild.Translation.archivingDate,
						dataIndex: CMDBuild.core.constants.Proxy.DATE,
						flex: 1
					},
					{
						text: CMDBuild.Translation.from,
						dataIndex: CMDBuild.core.constants.Proxy.FROM,
						flex: 1
					},
					{
						text: CMDBuild.Translation.to,
						dataIndex: CMDBuild.core.constants.Proxy.TO,
						flex: 1
					},
					{
						text: CMDBuild.Translation.subject,
						sortable: false,
						dataIndex: CMDBuild.core.constants.Proxy.SUBJECT,
						flex: 1
					},
					{
						sortable: false,
						scope: this,
						dataIndex: CMDBuild.core.constants.Proxy.BODY,
						menuDisabled: true,
						hideable: false,
						renderer: 'stripTags',
						flex: 2
					},
					Ext.create('Ext.grid.column.Action', {
						align: 'center',
						width: 150,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.email.Regenerate', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.manualRegeneration,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onTabEmailGridRegenerationEmailButtonClick', record);
								},

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
										|| !this.delegate.cmfg('tabEmailEditModeGet')
										|| !this.delegate.cmfg('tabEmailGridRecordIsRegenerable', record)
										|| !this.delegate.cmfg('tabEmailGridRecordIsEditable', record)
										|| !record.get(CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION)
									);
								}
							}),
							Ext.create('CMDBuild.core.buttons.email.Reply', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.reply,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onTabEmailGridReplyEmailButtonClick', record);
								},

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
										|| !this.delegate.cmfg('tabEmailEditModeGet')
										|| this.delegate.cmfg('tabEmailGridRecordIsEditable', record)
									);
								}
							}),
							Ext.create('CMDBuild.core.buttons.email.Send', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.send,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onTabEmailGridSendEmailButtonClick', record);
								},

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
										|| !this.delegate.cmfg('tabEmailEditModeGet')
										|| !this.delegate.cmfg('tabEmailGridRecordIsSendable', record)
									);
								}
							}),
							Ext.create('CMDBuild.core.buttons.email.Edit', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.edit,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onTabEmailGridEditEmailButtonClick', record);
								},

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
										|| !this.delegate.cmfg('tabEmailEditModeGet')
										|| !this.delegate.cmfg('tabEmailGridRecordIsEditable', record)
									);
								}
							}),
							Ext.create('CMDBuild.core.buttons.email.View', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.view,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onTabEmailGridViewEmailButtonClick', record);
								}
							}),
							Ext.create('CMDBuild.core.buttons.email.Delete', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.remove,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onTabEmailGridDeleteEmailButtonClick', record);
								},

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return (
										this.delegate.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
										|| !this.delegate.cmfg('tabEmailEditModeGet')
										|| !this.delegate.cmfg('tabEmailGridRecordIsEditable', record)
									);
								}
							})
						]
					})
				],
				features: [
					{
						ftype: 'groupingsummary',
						groupHeaderTpl: [
							'{name:this.formatName}',
							{
								formatName: function (name) { // TODO: use plain translation without emailLookupNames
									return CMDBuild.Translation.emailLookupNames[name];
								}
							}
						],
						hideGroupedHeader: true,
						enableGroupingMenu: false
					}
				],
				store: CMDBuild.proxy.common.tabs.email.Email.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onTabEmailGridItemDoubleClick', record);
			}
		}
	});

})();
