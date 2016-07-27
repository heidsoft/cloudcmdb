(function () {

	Ext.define('CMDBuild.view.management.widget.customForm.layout.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.layout.Form}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Export}
		 */
		exportButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Import}
		 */
		importButton: undefined,

		bodyCls: 'x-panel-body-default-framed',
		bodyPadding: 5,
		border: false,
		frame: false,
		overflowY: 'auto',

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
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							this.importButton = Ext.create('CMDBuild.core.buttons.iconized.Import', {
								text: CMDBuild.Translation.import,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutFormImportButtonClick');
								}
							}),
							this.exportButton = Ext.create('CMDBuild.core.buttons.iconized.Export', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutFormExportButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Reload', {
								text: CMDBuild.Translation.resetToDefault,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormResetButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
