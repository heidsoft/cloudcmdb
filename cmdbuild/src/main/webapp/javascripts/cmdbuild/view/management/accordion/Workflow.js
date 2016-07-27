(function () {

	Ext.define('CMDBuild.view.management.accordion.Workflow', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Workflow}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.processes,

		listeners: {
			collapse: function (panel, eOpts) {
				this.delegate.cmfg('onAccordionWorkflowCollapse');
			}
		}
	});

})();
