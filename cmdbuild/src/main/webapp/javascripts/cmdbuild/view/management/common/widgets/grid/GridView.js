(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.GridView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Grid}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		addButton: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		importFromCSVButton: undefined,

		autoScroll: true,
		border: false,
		frame: false,
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [
							this.addButton = Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addRow,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onAddRowButtonClick');
								}
							}),
							this.importFromCSVButton = Ext.create('CMDBuild.core.buttons.iconized.Import', {
								text: CMDBuild.Translation.importFromCSV,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onCSVImportButtonClick');
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