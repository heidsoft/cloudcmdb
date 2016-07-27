(function() {

	// Wizard task tabs index
	Ext.define('CMDBuild.view.administration.tasks.connector.CMTaskSteps', {

		/**
		 * @return {Array} task steps
		 */
		constructor: function() {
			return [
				Ext.create('CMDBuild.view.administration.tasks.connector.CMStep1'),
				Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration'),
				Ext.create('CMDBuild.view.administration.tasks.connector.CMStep3'),
				Ext.create('CMDBuild.view.administration.tasks.connector.CMStep4'),
				Ext.create('CMDBuild.view.administration.tasks.connector.CMStep5')
// TODO: future implementation
//				,
//				Ext.create('CMDBuild.view.administration.tasks.connector.CMStep6')
			];
		}
	});

})();
