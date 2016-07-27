(function() {

	Ext.define('CMDBuild.view.administration.configuration.RelationGraphPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.RelationGraph}
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
									this.delegate.cmfg('onConfigurationRelationGraphSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationRelationGraphAbortButtonClick');
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
							Ext.create('Ext.form.field.Checkbox',{
								name: CMDBuild.core.constants.Proxy.ENABLED,
								fieldLabel: CMDBuild.Translation.enabled,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Number',{
								name: CMDBuild.core.constants.Proxy.BASE_LEVEL,
								fieldLabel: CMDBuild.Translation.baseLevel,
								allowBlank: false,
								minValue: 1,
								maxValue: 5
							}),
							Ext.create('Ext.form.field.Number',{
								name: CMDBuild.core.constants.Proxy.CLUSTERING_THRESHOLD,
								fieldLabel: CMDBuild.Translation.thresholdForClusteringNodes,
								allowBlank: false,
								minValue: 2
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.informationLayer,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Checkbox',{
								name: CMDBuild.core.constants.Proxy.ENABLE_NODE_TOOLTIP,
								fieldLabel: CMDBuild.Translation.enableNodeTooltip,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox',{
								name: CMDBuild.core.constants.Proxy.ENABLE_EDGE_TOOLTIP,
								fieldLabel: CMDBuild.Translation.enableEdgeTooltip,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.DISPLAY_LABEL,
								fieldLabel: CMDBuild.Translation.displayNodeLabel,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM,
								valueField: CMDBuild.core.constants.Proxy.ID,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								editable: false,

								store: Ext.create('Ext.data.ArrayStore', {
									fields: [CMDBuild.core.constants.Proxy.ID, CMDBuild.core.constants.Proxy.DESCRIPTION],
									data: [
										[CMDBuild.core.constants.Proxy.NONE, CMDBuild.Translation.none],
										[CMDBuild.core.constants.Proxy.ALL, CMDBuild.Translation.all],
										[CMDBuild.core.constants.Proxy.SELECTED, CMDBuild.Translation.selected]
									]
								}),
								queryMode: 'local'
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.graphicParameters,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							Ext.create('CMDBuild.view.common.field.slider.SingleWithExtremeLabels', {
								name: CMDBuild.core.constants.Proxy.VIEW_POINT_HEIGHT,
								fieldLabel: CMDBuild.Translation.viewPointHeight,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
								minLabel: CMDBuild.Translation.min,
								minValue: 1,
								maxLabel: CMDBuild.Translation.max,
								maxValue: 100,
								useTips: false
							}),
							Ext.create('CMDBuild.view.common.field.slider.SingleWithExtremeLabels', {
								name: CMDBuild.core.constants.Proxy.VIEW_POINT_DISTANCE,
								fieldLabel: CMDBuild.Translation.viewPointDistance,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
								minLabel: CMDBuild.Translation.min,
								minValue: 1,
								maxLabel: CMDBuild.Translation.max,
								maxValue: 100,
								useTips: false
							}),
							Ext.create('Ext.form.field.Number',{
								name: CMDBuild.core.constants.Proxy.STEP_RADIUS,
								fieldLabel: CMDBuild.Translation.nodeDistance,
								allowBlank: false,
								minValue: 1
							}),
							Ext.create('Ext.form.field.Number',{
								name: CMDBuild.core.constants.Proxy.SPRITE_DIMENSION,
								fieldLabel: CMDBuild.Translation.spriteDimension,
								allowBlank: false,
								minValue: 1,
								maxValue: 1000
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.theme,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							Ext.create('CMDBuild.view.common.field.picker.Color', {
								name: CMDBuild.core.constants.Proxy.EDGE_COLOR,
								fieldLabel: CMDBuild.Translation.edgeColor
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationRelationGraphTabShow');
			}
		}
	});

})();