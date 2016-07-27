(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @administration
	 *
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.widget.calendar.Definition', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_DATE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.END_DATE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EVENT_CLASS, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EVENT_TITLE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.START_DATE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: '.Calendar' }
		],

		statics: {
			/**
			 * Static function to convert from legacy object to model's one
			 *
			 * @param {Object} data
			 *
			 * @returns {Object} data
			 */
			convertFromLegacy: function(data) {
				data = data || {};

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED]))
					data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED] = data['alwaysenabled'];

				return data;
			},

			/**
			 * Static function to convert from model's object to legacy one
			 *
			 * @returns {Object}
			 */
			convertToLegacy: function(data) {
				return {
					active: data[CMDBuild.core.constants.Proxy.ACTIVE],
					alwaysenabled: data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED],
					defaultDate: data[CMDBuild.core.constants.Proxy.DEFAULT_DATE],
					endDate: data[CMDBuild.core.constants.Proxy.END_DATE],
					eventClass: data[CMDBuild.core.constants.Proxy.EVENT_CLASS],
					eventTitle: data[CMDBuild.core.constants.Proxy.EVENT_TITLE],
					filter: data[CMDBuild.core.constants.Proxy.FILTER],
					id: data[CMDBuild.core.constants.Proxy.ID],
					label: data[CMDBuild.core.constants.Proxy.LABEL],
					startDate: data[CMDBuild.core.constants.Proxy.START_DATE],
					type: data[CMDBuild.core.constants.Proxy.TYPE]
				};
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function(data) {
			data = CMDBuild.model.widget.calendar.Definition.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();