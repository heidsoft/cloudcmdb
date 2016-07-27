(function() {

	Ext.define('CMDBuild.view.administration.accordion.UserAndGroup', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.UserAndGroup}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.usersAndGroups
	});

})();