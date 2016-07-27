(function () {

	Ext.define('CMDBuild.view.management.accordion.Menu', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.menu.accordion.Management'],

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Menu}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.menu.accordion.Management',

		title: CMDBuild.Translation.navigation
	});

})();
