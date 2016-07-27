(function() {

	Ext.define('CMDBuild.view.administration.accordion.Gis', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Gis}
		 */
		delegate: undefined,

		disabled: (
			!CMDBuild.configuration.gis.get(CMDBuild.core.constants.Proxy.ENABLED)
			|| CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN)
		),
		title: CMDBuild.Translation.gis
	});

})();