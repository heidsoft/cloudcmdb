(function() {

	Ext.define('CMDBuild.controller.common.field.delay.Delay', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getDelayStore',
			'onDelayBeforeSelect'
		],

		/**
		 * @property {CMDBuild.view.common.field.delay.Delay}
		 */
		view: undefined,

		/**
		 * This should be in proxy, maybe in future
		 *
		 * @return {Ext.data.Store}
		 */
		getDelayStore: function() {
			return Ext.create('Ext.data.Store', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					{ value: 0, description: CMDBuild.Translation.delayLabels.none },
					{ value: 3600000, description: CMDBuild.Translation.delayLabels.hour1 },
					{ value: 7200000, description: CMDBuild.Translation.delayLabels.hours2 },
					{ value: 14400000, description: CMDBuild.Translation.delayLabels.hours4 },
					{ value: null, description: null }, // Separator
					{ value: 86400000, description: CMDBuild.Translation.delayLabels.day1 },
					{ value: 172800000, description: CMDBuild.Translation.delayLabels.days2 },
					{ value: 345600000, description: CMDBuild.Translation.delayLabels.days4 },
					{ value: null, description: null }, // Separator
					{ value: 604800017, description: CMDBuild.Translation.delayLabels.week1 },
					{ value: 1209600033, description: CMDBuild.Translation.delayLabels.weeks2 },
					{ value: null, description: null }, // Separator
					{ value: 2629800000, description: CMDBuild.Translation.delayLabels.month1 }
				]
			});
		},

		/**
		 * @param {Ext.data.Model} record
		 *
		 * @return {Boolean}
		 */
		onDelayBeforeSelect: function(record) {
			if (Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE)))
				return false;
		}
	});

})();
