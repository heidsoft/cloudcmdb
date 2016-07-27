(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.Domain', { // TODO: waiting for refactor (rename)
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.CARDINALITY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_DISABLED_CLASSES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.IS_MASTER_DETAIL, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.MASTER_DETAIL_LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ORIGIN_DISABLED_CLASSES, type: 'auto', defaultValue: [] }
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
				data[CMDBuild.core.constants.Proxy.DESTINATION_DISABLED_CLASSES] = data['disabled2'];
				data[CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION] = data['descrdir'];
				data[CMDBuild.core.constants.Proxy.ID] = data['idDomain'];
				data[CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION] = data['descrinv'];
				data[CMDBuild.core.constants.Proxy.IS_MASTER_DETAIL] = data['md'];
				data[CMDBuild.core.constants.Proxy.MASTER_DETAIL_LABEL] = data['md_label'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID] = data['class1id'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME] = data['class1'];
				data[CMDBuild.core.constants.Proxy.ORIGIN_DISABLED_CLASSES] = data['disabled1'];

				return data;
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			data = this.statics().convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();
