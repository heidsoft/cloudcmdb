(function() {

	Ext.define('CMDBuild.view.administration.accordion.Domain', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Domain}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.domains
	});

})();