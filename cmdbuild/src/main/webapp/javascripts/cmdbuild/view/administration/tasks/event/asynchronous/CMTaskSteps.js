(function() {

	// Wizard task tabs index
	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMTaskSteps', {

		/**
		 * @return {Array} task steps
		 */
		constructor: function() {
			return [
				Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1'),
				Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep2'),
				Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration'),
				Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep4')
			];
		}
	});

})();