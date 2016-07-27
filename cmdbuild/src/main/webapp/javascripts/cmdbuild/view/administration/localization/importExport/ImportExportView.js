(function () {

	Ext.define('CMDBuild.view.administration.localization.importExport.ImportExportView', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localization.ImportExport}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ExportForm}
		 */
		exportPanel: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ImportForm}
		 */
		importPanel: undefined,

		activeTab: 0,
		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,

		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.importPanel = Ext.create('CMDBuild.view.administration.localization.importExport.ImportForm', { delegate: this.delegate }),
					this.exportPanel = Ext.create('CMDBuild.view.administration.localization.importExport.ExportForm', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
