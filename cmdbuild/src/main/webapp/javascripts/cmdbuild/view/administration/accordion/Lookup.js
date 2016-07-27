(function() {

	Ext.define('CMDBuild.view.administration.accordion.Lookup', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.lookup.accordion.Administration'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Lookup}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.lookup.accordion.Administration',

		title: CMDBuild.Translation.lookupTypes
	});

})();