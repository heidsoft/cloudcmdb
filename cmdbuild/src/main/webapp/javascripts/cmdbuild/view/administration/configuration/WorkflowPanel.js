(function() {

	Ext.define('CMDBuild.view.administration.configuration.WorkflowPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Workflow}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		fieldDefaults: {
			labelAlign: 'left',
			labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION,
			maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM
		},

		initComponent: function() {
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

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationWorkflowSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationWorkflowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.general,
						autoHeight: true,
						defaultType: 'textfield',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.ENABLED,
								fieldLabel: CMDBuild.Translation.enabled,
								inputValue: true,
								uncheckedValue: false
							}),
							{
								fieldLabel: CMDBuild.Translation.serverUrl,
								name: CMDBuild.core.constants.Proxy.URL,
								allowBlank: false,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
							},
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.ENABLE_ADD_ATTACHMENT_ON_CLOSED_ACTIVITIES,
								fieldLabel: CMDBuild.Translation.enableAddAttachmentOnClosedActivities,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.DISABLE_SYNCHRONIZATION_OF_MISSING_VARIABLES,
								fieldLabel: CMDBuild.Translation.disableSynchronizationOfMissingVariables,
								inputValue: true,
								uncheckedValue: false
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.credentials,
						autoHeight: true,
						defaultType: 'textfield',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								fieldLabel: CMDBuild.Translation.username,
								name: CMDBuild.core.constants.Proxy.USER,
								allowBlank: false
							},
							{
								fieldLabel: CMDBuild.Translation.password,
								name: CMDBuild.core.constants.Proxy.PASSWORD,
								allowBlank: false,
								inputType: 'password'
							},
							{
								fieldLabel: CMDBuild.Translation.engineName,
								name: CMDBuild.core.constants.Proxy.ENGINE,
								allowBlank: false,
								disabled: true
							},
							{
								fieldLabel: CMDBuild.Translation.scope,
								name: CMDBuild.core.constants.Proxy.SCOPE,
								allowBlank: true,
								disabled: true
							}
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationWorkflowTabShow');
			}
		}
	});

})();