(function() {

	Ext.define('CMDBuild.view.management.report.SinglePanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.report.Single}
		 */
		delegate: undefined,

		/**
		 * @param {Number}
		 */
		reportId: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.report,

		border: true,
		frame: false,
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.fileTypes.Pdf', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.constants.Proxy.PDF);
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Odt', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.constants.Proxy.ODT);
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Rtf', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.constants.Proxy.RTF);
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Csv', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportTypeButtonClick', CMDBuild.core.constants.Proxy.CSV);
								}
							}),
							'->',
							Ext.create('CMDBuild.core.buttons.iconized.Download', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onSingleReportDownloadButtonClick');
								}
							})
						]
					})
				],
				tools: [
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Properties', {
						style: {} // Reset margin setup
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();