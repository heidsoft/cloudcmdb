(function () {

	Ext.define('CMDBuild.view.management.widget.customForm.export.ExportWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.Export}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.widget.customForm.export.FormPanel}
		 */
		form: undefined,

		autoHeight: true,
		border: false,
		defaultSizeW: 0.90,
		title: CMDBuild.Translation.exportLabel,

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
							Ext.create('CMDBuild.core.buttons.text.Export', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormExportExportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormExportAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('CMDBuild.view.management.widget.customForm.export.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.width = this.width * this.defaultSizeW;
		}
	});

})();
