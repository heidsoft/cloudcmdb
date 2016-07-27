(function () {

	Ext.define('CMDBuild.view.management.widget.customForm.layout.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.layout.Grid}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.add.Add}
		 */
		addButton: undefined,

		/**
		 * @cfg {Array}
		 */
		columns: [],

		/**
		 * @property {CMDBuild.core.buttons.iconized.Export}
		 */
		exportButton: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Import}
		 */
		importButton: undefined,

		border: false,
		frame: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			var isWidgetReadOnly = this.delegate.cmfg('widgetCustomFormConfigurationGet', [
				CMDBuild.core.constants.Proxy.CAPABILITIES,
				CMDBuild.core.constants.Proxy.READ_ONLY
			]);

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							this.addButton = Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addRow,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutGridAddRowButtonClick');
								}
							}),
							this.importButton = Ext.create('CMDBuild.core.buttons.iconized.Import', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutGridImportButtonClick');
								}
							}),
							this.exportButton = Ext.create('CMDBuild.core.buttons.iconized.Export', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetCustomFormLayoutGridExportButtonClick');
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
				],
				plugins: (
					isWidgetReadOnly
					|| this.delegate.cmfg('widgetCustomFormConfigurationGet', [
						CMDBuild.core.constants.Proxy.CAPABILITIES,
						CMDBuild.core.constants.Proxy.MODIFY_DISABLED
					])
				) ? [] : [this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })]
			});

			this.callParent(arguments);
		}
	});

})();
