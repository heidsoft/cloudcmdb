(function() {

	Ext.define('CMDBuild.view.administration.accordion.Localization', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Localization}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.localizations
	});

})();