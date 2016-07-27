(function() {

	// Wizard task tabs index
	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMTaskSteps', {

		/**
		 * @return {Array} task steps
		 */
		constructor: function() {
			return [
				Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep1'),
				Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep2'),
				Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep3')
			];
		}
	});

})();