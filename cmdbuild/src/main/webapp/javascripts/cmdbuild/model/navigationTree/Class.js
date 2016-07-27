(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.navigationTree.Class', { // TODO: waiting for refactor (rename and structure)
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'int', useNull: true }
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
				data[CMDBuild.core.constants.Proxy.DESCRIPTION] = data['text'];

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
