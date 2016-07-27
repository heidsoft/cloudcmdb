(function () {

	Ext.define('CMDBuild.view.management.utility.bulkUpdate.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.utility.bulkUpdate.BulkUpdate}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		}
	});

})();
