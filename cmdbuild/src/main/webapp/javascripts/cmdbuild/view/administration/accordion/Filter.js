(function() {

	Ext.define('CMDBuild.view.administration.accordion.Filter', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Filter}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.searchFilters
	});

})();