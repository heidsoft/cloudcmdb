(function() {

	// Wizard task tabs index
	Ext.define('CMDBuild.view.administration.tasks.email.CMTaskSteps', {

		/**
		 * @return {Array} task steps
		 */
		constructor: function() {
			return [
				Ext.create('CMDBuild.view.administration.tasks.email.CMStep1'),
				Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration'),
				Ext.create('CMDBuild.view.administration.tasks.email.CMStep3'),
				Ext.create('CMDBuild.view.administration.tasks.email.CMStep4')
			];
		}
	});

})();
