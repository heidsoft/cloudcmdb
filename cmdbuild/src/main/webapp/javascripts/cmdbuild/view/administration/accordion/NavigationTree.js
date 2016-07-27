(function() {

	Ext.define('CMDBuild.view.administration.accordion.NavigationTree', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		requires: ['CMDBuild.model.navigationTree.accordion.Administration'],

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.NavigationTree}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		storeModelName: 'CMDBuild.model.navigationTree.accordion.Administration',

		title: CMDBuild.Translation.navigationTrees
	});

})();