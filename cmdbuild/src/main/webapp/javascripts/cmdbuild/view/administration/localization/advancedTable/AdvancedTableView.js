(function () {

	Ext.define('CMDBuild.view.administration.localization.advancedTable.AdvancedTableView', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable}
		 */
		delegate: undefined,

		activeTab: 0,
		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onLocalizationAdvancedTableShow');
			}
		}
	});

})();
