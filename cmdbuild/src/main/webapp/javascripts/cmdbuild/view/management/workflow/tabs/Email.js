(function () {

	/**
	 * Extends original view to implement function to select tab on widget button click
	 */
	Ext.define('CMDBuild.view.management.workflow.tabs.Email', {
		extend: 'CMDBuild.view.management.common.tabs.email.EmailView',

		requires: ['CMDBuild.core.constants.Proxy'],


		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		delegate: undefined,

		cmActivate: function() {
			this.setDisabled(false);

			this.delegate.parentDelegate.view.cardTabPanel.acutalPanel.setActiveTab(this);
		}
	});

})();