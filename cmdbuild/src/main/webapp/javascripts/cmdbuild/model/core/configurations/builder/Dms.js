(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * CMDBuild configuration object
	 *
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.core.configurations.builder.Dms', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ALFRESCO_DELAY, type: 'int', defaultValue: 1000, useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
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
				data[CMDBuild.core.constants.Proxy.ALFRESCO_DELAY] = data['delay'];
				data[CMDBuild.core.constants.Proxy.TYPE] = data['dms.service.type'];

				return data;
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function (data) {
			data = this.statics().convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();
