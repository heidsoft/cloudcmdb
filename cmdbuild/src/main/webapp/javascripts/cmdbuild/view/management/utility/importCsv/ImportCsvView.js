(function () {

	Ext.define('CMDBuild.view.management.utility.importCsv.ImportCsvView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.utility.importCsv.ImportCsv}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.importCsv.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.importCsv.GridPanel}
		 */
		grid: undefined,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: false,
		frame: false,
		layout: 'border',

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
							Ext.create('CMDBuild.core.buttons.text.Update', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityImportCsvUpdateButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityImportCsvConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityImportCsvAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('CMDBuild.view.management.utility.importCsv.FormPanel', {
						delegate: this.delegate,
						region: 'north',
						height: '40%',
						split: true
					}),
					this.grid = Ext.create('CMDBuild.view.management.utility.importCsv.GridPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
