(function() {

	Ext.define('CMDBuild.view.administration.configuration.GeneralOptionsPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.configuration.GeneralOptions'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.GeneralOptions}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		instanceNameField: undefined,

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
									this.delegate.cmfg('onConfigurationGeneralOptionsSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationGeneralOptionsAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.general,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							this.instanceNameField = Ext.create('CMDBuild.view.common.field.translatable.Text', {
								name: CMDBuild.core.constants.Proxy.INSTANCE_NAME,
								fieldLabel: CMDBuild.Translation.instanceName,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
								allowBlank: true,

								translationFieldConfig: {
									type: CMDBuild.core.constants.Proxy.INSTANCE_NAME,
									identifier: CMDBuild.core.constants.Proxy.INSTANCE_NAME, // Just for configuration validation
									field: CMDBuild.core.constants.Proxy.INSTANCE_NAME
								}
							}),
							Ext.create('CMDBuild.view.common.field.comboBox.Erasable', {
								name: CMDBuild.core.constants.Proxy.STARTING_CLASS,
								fieldLabel: CMDBuild.Translation.defaultClass,
								valueField: CMDBuild.core.constants.Proxy.ID,
								displayField: CMDBuild.core.constants.Proxy.TEXT,
								forceSelection: true,

								store: CMDBuild.proxy.configuration.GeneralOptions.getStoreStartingClass(),
								queryMode: 'local'
							}),
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.ROW_LIMIT,
								fieldLabel: CMDBuild.Translation.rowLimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT,
								fieldLabel: CMDBuild.Translation.referenceComboLimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.RELATION_LIMIT,
								fieldLabel: CMDBuild.Translation.relationLimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.CARD_FORM_RATIO,
								fieldLabel: CMDBuild.Translation.cardPanelHeight,
								allowBlank: false,
								maxValue: 100,
								minValue: 0
							},
							{
								xtype: 'combobox',
								name: CMDBuild.core.constants.Proxy.CARD_TABS_POSITION,
								fieldLabel: CMDBuild.Translation.tabPositioInCardPanel,
								allowBlank: false,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								valueField: CMDBuild.core.constants.Proxy.VALUE,

								store: Ext.create('Ext.data.ArrayStore', {
									fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
									data: [
										['top', CMDBuild.Translation.top],
										['bottom', CMDBuild.Translation.bottom]
									]
								}),
								queryMode: 'local'
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.SESSION_TIMEOUT,
								fieldLabel: CMDBuild.Translation.sessionTimeout,
								allowBlank: true,
								minValue: 0
							}
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.popupWindows,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE,
								fieldLabel: CMDBuild.Translation.popupPercentageHeight,
								maxValue: 100,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE,
								fieldLabel: CMDBuild.Translation.popupPercentageWidth,
								maxValue: 100,
								allowBlank: false
							}
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.lockCardsAndProcessesInEdit,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								xtype: 'checkbox',
								name: CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK,
								fieldLabel: CMDBuild.Translation.enabled,
								inputValue: true,
								uncheckedValue: false
							},
							{
								xtype: 'checkbox',
								name: CMDBuild.core.constants.Proxy.DISPLAY_CARD_LOCKER_NAME,
								fieldLabel: CMDBuild.Translation.showLockerUserName,
								inputValue: true,
								uncheckedValue: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.CARD_LOCK_TIMEOUT,
								fieldLabel: CMDBuild.Translation.lockTimeout
							}
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationGeneralOptionsTabShow');
			}
		}
	});

})();