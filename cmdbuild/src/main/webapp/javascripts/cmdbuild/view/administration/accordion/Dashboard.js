(function() {

	Ext.define('CMDBuild.view.administration.accordion.Dashboard', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Dashboard}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.dashboard
	});

})();