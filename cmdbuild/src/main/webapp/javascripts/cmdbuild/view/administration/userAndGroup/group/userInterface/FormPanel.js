(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.userInterface.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.UserInterface}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
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
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupTabUserInterfaceSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupTabUserInterfaceAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmdb-gray-panel',
						cls: 'cmdb-border-bottom',
						frame: false,
						border: false,

						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.disabledFeatures,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.cards,
										name: CMDBuild.core.constants.Proxy.CLASS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.processes,
										name: CMDBuild.core.constants.Proxy.PROCESS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.views,
										name: CMDBuild.core.constants.Proxy.DATA_VIEW,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.dashboard,
										name: CMDBuild.core.constants.Proxy.DASHBOARD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.report,
										name: CMDBuild.core.constants.Proxy.REPORT,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.customPages,
										name: CMDBuild.core.constants.Proxy.CUSTOM_PAGES,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityChangePassword,
										name: CMDBuild.core.constants.Proxy.CHANGE_PASSWORD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityMultipleUpdate,
										name: CMDBuild.core.constants.Proxy.BULK_UPDATE,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityImportCsv,
										name: CMDBuild.core.constants.Proxy.IMPORT_CSV,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.utilityExportCsv,
										name: CMDBuild.core.constants.Proxy.EXPORT_CSV,
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					}),
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmdb-gray-panel',
						cls: 'cmdb-border-bottom',
						frame: false,
						border: false,

						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.disabledTabsInClassesModule,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.detail,
										name: CMDBuild.core.constants.Proxy.CLASS_DETAIL_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.notes,
										name: CMDBuild.core.constants.Proxy.CLASS_NOTE_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.relations,
										name: CMDBuild.core.constants.Proxy.CLASS_RELATION_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.history,
										name: CMDBuild.core.constants.Proxy.CLASS_HISTORY_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.email,
										name: CMDBuild.core.constants.Proxy.CLASS_EMAIL_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.attachments,
										name: CMDBuild.core.constants.Proxy.CLASS_ATTACHMENT_TAB,
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					}),
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmdb-gray-panel',
						cls: 'cmdb-border-bottom',
						frame: false,
						border: false,

						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.disabledTabsInProcessesModule,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.notes,
										name: CMDBuild.core.constants.Proxy.PROCESS_NOTE_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.relations,
										name: CMDBuild.core.constants.Proxy.PROCESS_RELATION_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.history,
										name: CMDBuild.core.constants.Proxy.PROCESS_HISTORY_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.email,
										name: CMDBuild.core.constants.Proxy.PROCESS_EMAIL_TAB,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.attachments,
										name: CMDBuild.core.constants.Proxy.PROCESS_ATTACHMENT_TAB,
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					}),
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmdb-gray-panel',
						cls: 'cmdb-border-bottom',
						frame: false,
						border: false,

						items: [
							Ext.create('Ext.form.CheckboxGroup', {
								fieldLabel: CMDBuild.Translation.otherOptions,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								columns: 1,
								items: [
									{
										boxLabel: CMDBuild.Translation.hideSidePanel,
										name: CMDBuild.core.constants.Proxy.HIDE_SIDE_PANEL,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.fullScreenNavigation,
										name: CMDBuild.core.constants.Proxy.FULL_SCREEN_MODE,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.simpleHistoryForCards,
										name: CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_CARD,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.simpleHistoryForProcesses,
										name: CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_PROCESS,
										inputValue: true,
										uncheckedValue: false
									},
									{
										boxLabel: CMDBuild.Translation.processWidgetsAlwaysEnabled,
										name: CMDBuild.core.constants.Proxy.PROCESS_WIDGET_ALWAYS_ENABLED,
										inputValue: true,
										uncheckedValue: false
									}
								]
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
