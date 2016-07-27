(function() {

	Ext.define('CMDBuild.view.administration.accordion.Configuration', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Configuration}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.setup
	});

})();