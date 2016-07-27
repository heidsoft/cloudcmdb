(function() {

	Ext.define('CMDBuild.view.administration.configuration.GisPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Gis}
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
									this.delegate.cmfg('onConfigurationGisSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationGisAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					{
						xtype: 'checkbox',
						name: CMDBuild.core.constants.Proxy.ENABLED,
						fieldLabel: CMDBuild.Translation.enable,
						inputValue: true,
						uncheckedValue: false
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.constants.Proxy.CENTER_LATITUDE,
						decimalPrecision: 6,
						fieldLabel: CMDBuild.Translation.initialLatitude
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.constants.Proxy.CENTER_LONGITUDE,
						decimalPrecision: 6,
						fieldLabel: CMDBuild.Translation.initialLongitude
					},
					{
						xtype: 'numberfield',
						name: CMDBuild.core.constants.Proxy.ZOOM_INITIAL_LEVEL,
						fieldLabel: CMDBuild.Translation.initialZoomLevel,
						minValue: 0,
						maxValue: 25
					}
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationGisTabShow');
			}
		}
	});

})();