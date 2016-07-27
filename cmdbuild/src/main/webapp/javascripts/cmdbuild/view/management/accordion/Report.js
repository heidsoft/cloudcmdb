(function () {

	Ext.define('CMDBuild.view.management.accordion.Report', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.report.accordion.Management'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Report}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.report.accordion.Management',

		title: CMDBuild.Translation.report
	});

})();
