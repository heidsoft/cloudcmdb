(function() {

	Ext.define('CMDBuild.view.administration.accordion.Workflow', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Workflow}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.processes
	});

})();