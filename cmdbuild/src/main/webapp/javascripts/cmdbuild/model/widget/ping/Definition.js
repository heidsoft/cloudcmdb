(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @administration
	 *
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.widget.ping.Definition', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.COUNT, type: 'int', defaultValue: 3 },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATES, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: '.Ping' }
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
					address: data[CMDBuild.core.constants.Proxy.ADDRESS],
					alwaysenabled: data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED],
					count: data[CMDBuild.core.constants.Proxy.COUNT],
					id: data[CMDBuild.core.constants.Proxy.ID],
					label: data[CMDBuild.core.constants.Proxy.LABEL],
					templates: data[CMDBuild.core.constants.Proxy.TEMPLATES],
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
			data = CMDBuild.model.widget.ping.Definition.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();