(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @administration
	 *
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.widget.workflow.Definition', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PRESET, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: '.Workflow' },
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_NAME, type: 'string' }
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
					filter: data[CMDBuild.core.constants.Proxy.FILTER],
					filterType: data[CMDBuild.core.constants.Proxy.FILTER_TYPE],
					id: data[CMDBuild.core.constants.Proxy.ID],
					label: data[CMDBuild.core.constants.Proxy.LABEL],
					preset: data[CMDBuild.core.constants.Proxy.PRESET],
					type: data[CMDBuild.core.constants.Proxy.TYPE],
					workflowName: data[CMDBuild.core.constants.Proxy.WORKFLOW_NAME]
				};
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function(data) {
			data = CMDBuild.model.widget.workflow.Definition.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();