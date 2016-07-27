(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.workflow.Workflow', { // TODO: waiting for refactor (rename and structure)
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.CAPABILITIES, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IS_SUPER_CLASS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.META, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.PERMISSIONS, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.TABLE_TYPE, type: 'string', defaultValue: 'standard' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: 'processclass' },
			{ name: CMDBuild.core.constants.Proxy.USER_STOPPABLE, type: 'boolean' }
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
				data[CMDBuild.core.constants.Proxy.CAPABILITIES] = data['ui_card_edit_mode'];
				data[CMDBuild.core.constants.Proxy.DESCRIPTION] = data['text'] || data[CMDBuild.core.constants.Proxy.DESCRIPTION];
				data[CMDBuild.core.constants.Proxy.IS_SUPER_CLASS] = data['superclass'] || data[CMDBuild.core.constants.Proxy.IS_SUPER_CLASS];
				data[CMDBuild.core.constants.Proxy.PERMISSIONS] = {
					create: data['priv_create'],
					write: data['priv_write']
				};
				data[CMDBuild.core.constants.Proxy.USER_STOPPABLE] = data['userstoppable'] || data[CMDBuild.core.constants.Proxy.USER_STOPPABLE];

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
			data = this.self.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();
