(function() {

	Ext.define('CMDBuild.view.administration.configuration.BimPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Bim}
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
									this.delegate.cmfg('onConfigurationBimSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationBimAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					{
						xtype: 'checkbox',
						name: CMDBuild.core.constants.Proxy.ENABLED,
						fieldLabel: CMDBuild.Translation.enabled,
						inputValue: true,
						uncheckedValue: false
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.constants.Proxy.URL,
						fieldLabel: CMDBuild.Translation.url,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.constants.Proxy.USERNAME,
						fieldLabel: CMDBuild.Translation.username
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.constants.Proxy.PASSWORD,
						fieldLabel: CMDBuild.Translation.password,
						inputType: 'password'
					}
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationBimTabShow');
			}
		}
	});

})();