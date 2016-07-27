(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @twin {CMDBuild.model.userAndGroup.group.userInterface.DisabledModules}
	 */
	Ext.define('CMDBuild.model.core.configurations.builder.userInterface.DisabledModules', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.BULK_UPDATE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CHANGE_PASSWORD, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CUSTOM_PAGES, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DASHBOARD, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DATA_VIEW, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.EXPORT_CSV, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IMPORT_CSV, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REPORT, type: 'boolean' }
		],

		statics: {
			/**
			 * 15/10/2015
			 * Static function to convert from legacy object to model's one (retrocompatibility to delete)
			 *
			 * @param {Object} data
			 *
			 * @returns {Object} data
			 */
			convertFromLegacy: function (data) {
				data = data || {};

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.BULK_UPDATE]))
					data[CMDBuild.core.constants.Proxy.BULK_UPDATE] = data['bulkupdate'];

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.EXPORT_CSV]))
					data[CMDBuild.core.constants.Proxy.EXPORT_CSV] = data['exportcsv'];

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.IMPORT_CSV]))
					data[CMDBuild.core.constants.Proxy.IMPORT_CSV] = data['importcsv'];

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

