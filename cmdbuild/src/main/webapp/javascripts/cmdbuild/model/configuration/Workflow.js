(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.configuration.Workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DISABLE_SYNCHRONIZATION_OF_MISSING_VARIABLES, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ENABLE_ADD_ATTACHMENT_ON_CLOSED_ACTIVITIES, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.URL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.USER, type: 'string' }
		],

		statics: {
			/**
			 * Static function to convert from legacy object to model's one
			 *
			 * @param {Object} data
			 *
			 * @returns {Object} data
			 */
			convertFromLegacy: function (data) {
				data = data || {};
				data[CMDBuild.core.constants.Proxy.URL] = data['endpoint'];

				return data;
			},

			/**
			 * Static function to convert from model's object to legacy one
			 *
			 * @returns {Object}
			 */
			convertToLegacy: function (data) {
				return {
					'endpoint': data[CMDBuild.core.constants.Proxy.URL],
					disableSynchronizationOfMissingVariables: data[CMDBuild.core.constants.Proxy.DISABLE_SYNCHRONIZATION_OF_MISSING_VARIABLES],
					enableAddAttachmentOnClosedActivities: data[CMDBuild.core.constants.Proxy.ENABLE_ADD_ATTACHMENT_ON_CLOSED_ACTIVITIES],
					enabled: data[CMDBuild.core.constants.Proxy.ENABLED],
					password: data[CMDBuild.core.constants.Proxy.PASSWORD],
					user: data[CMDBuild.core.constants.Proxy.USER]
				};
			}
		}
	});

})();
