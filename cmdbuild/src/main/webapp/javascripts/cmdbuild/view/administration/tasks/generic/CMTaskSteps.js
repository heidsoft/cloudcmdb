(function () {

	// Wizard task tabs index
	Ext.define('CMDBuild.view.administration.tasks.generic.CMTaskSteps', {

		/**
		 * @returns {Array}
		 */
		constructor: function () {
			return [
				Ext.create('CMDBuild.view.administration.tasks.generic.CMStep1'),
				Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration'),
				Ext.create('CMDBuild.view.administration.tasks.generic.CMStep3'),
				Ext.create('CMDBuild.view.administration.tasks.generic.CMStep4'),
				Ext.create('CMDBuild.view.administration.tasks.generic.CMStep5')
			];
		}
	});

})();
