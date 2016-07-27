(function() {

	Ext.require(['CMDBuild.core.constants.Proxy']);

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.lookup.Lookup', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.CODE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NOTES, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NUMBER, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.PARENT_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.TRANSLATION_UUID, type: 'string' }
		],

		statics: {
			/**
			 * Static function to create translated properties
			 *
			 * @returns {Object}
			 */
			convertFromLegacy: function(data) {
				data[CMDBuild.core.constants.Proxy.ACTIVE] = data['Active'];
				data[CMDBuild.core.constants.Proxy.CODE] = data['Code'];
				data[CMDBuild.core.constants.Proxy.DESCRIPTION] = data['Description'];
				data[CMDBuild.core.constants.Proxy.ID] = data['Id'];
				data[CMDBuild.core.constants.Proxy.NOTES] = data['Notes'];
				data[CMDBuild.core.constants.Proxy.NUMBER] = data['Number'];
				data[CMDBuild.core.constants.Proxy.PARENT_DESCRIPTION] = data['ParentDescription'];
				data[CMDBuild.core.constants.Proxy.PARENT_ID] = data['ParentId'];
				data[CMDBuild.core.constants.Proxy.TRANSLATION_UUID] = data['TranslationUuid'];

				return data;
			},

			/**
			 * Static function to convert from model's object to legacy one
			 *
			 * @returns {Object}
			 */
			convertToLegacy: function(data) {
				return {
					Active: data[CMDBuild.core.constants.Proxy.ACTIVE],
					Code: data[CMDBuild.core.constants.Proxy.CODE],
					Description: data[CMDBuild.core.constants.Proxy.DESCRIPTION],
					Id: data[CMDBuild.core.constants.Proxy.ID],
					Notes: data[CMDBuild.core.constants.Proxy.NOTES],
					Number: data[CMDBuild.core.constants.Proxy.NUMBER],
					ParentDescription: data[CMDBuild.core.constants.Proxy.PARENT_DESCRIPTION],
					ParentId: data[CMDBuild.core.constants.Proxy.PARENT_ID],
					TranslationUuid: data[CMDBuild.core.constants.Proxy.TRANSLATION_UUID]
				};
			}
		},

		/**
		 * @param {Array} data
		 *
		 * @override
		 */
		constructor: function(data) {
			if (!Ext.isEmpty(data) && !Ext.isEmpty(data['Id'])) // Legacy mode
				data = CMDBuild.model.lookup.Lookup.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();