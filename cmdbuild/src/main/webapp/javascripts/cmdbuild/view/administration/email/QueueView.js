(function() {

	Ext.define('CMDBuild.view.administration.email.QueueView', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Queue}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.slider.SingleWithExtremeLabels}
		 */
		cycleIntervalField: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		queueStartButton: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		queueStopButton: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							'->',
							this.queueStartButton = Ext.create('CMDBuild.core.buttons.iconized.Start', {
								text: CMDBuild.Translation.start,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailQueueStartButtonClick');
								}
							}),
							this.queueStopButton = Ext.create('CMDBuild.core.buttons.iconized.Stop', {
								text: CMDBuild.Translation.stop,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailQueueStopButtonClick');
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
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailQueueSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailQueueAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.cycleIntervalField = Ext.create('CMDBuild.view.common.field.slider.SingleWithExtremeLabels', {
						name: CMDBuild.core.constants.Proxy.TIME,
						fieldLabel: CMDBuild.Translation.frequencyCheck,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
						minValue: 1,
						maxValue: 10
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onEmailQueueShow');
			}
		}
	});

})();