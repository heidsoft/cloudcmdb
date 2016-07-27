(function() {

	// Wizard task tabs index
	Ext.define('CMDBuild.view.administration.tasks.workflow.CMTaskSteps', {

		/**
		 * @return {Array} task steps
		 */
		constructor: function() {
			return [
				Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep1'),
				Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration')
			];
		}
	});

})();
