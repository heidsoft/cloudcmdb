(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.navigationTree.Domain', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME, type: 'string' }
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
				data[CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID] = data['class2id'];
				data[CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME] = data['class2'];
				data[CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION] = data['descrdir'];
				data[CMDBuild.core.constants.Proxy.ID] = data['idDomain'];
				data[CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION] = data['descrinv'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID] = data['class1id'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME] = data['class1'];

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
