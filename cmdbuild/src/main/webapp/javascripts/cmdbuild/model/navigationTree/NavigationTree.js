(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.navigationTree.NavigationTree', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.BASE_NODE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CHILD_NODES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DIRECT, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TARGET_CLASS_DESCRIPTION, type: 'string' }, // TODO: verify
			{ name: CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME, type: 'string' }
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
				data[CMDBuild.core.constants.Proxy.NAME] = data['type']; // TODO: waiting for refactor

				return data;
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function(data) {
			data = this.statics().convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();
