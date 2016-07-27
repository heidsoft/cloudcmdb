(function() {

	Ext.define('CMDBuild.view.administration.accordion.Menu', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.menu.accordion.Administration'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Menu}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.menu.accordion.Administration',

		title: CMDBuild.Translation.menu
	});

})();