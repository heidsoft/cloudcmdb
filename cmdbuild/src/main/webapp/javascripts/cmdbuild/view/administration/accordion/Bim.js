(function() {

	Ext.define('CMDBuild.view.administration.accordion.Bim', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Bim}
		 */
		delegate: undefined,

		disabled: !CMDBuild.configuration.bim.get(CMDBuild.core.constants.Proxy.ENABLED),
		title: CMDBuild.Translation.bim
	});

})();