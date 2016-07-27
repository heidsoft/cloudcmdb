(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.widget.openReport.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.FORCE_FORMAT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PRESET, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY_ATTRIBUTES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.REPORT_CODE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: '.OpenReport' }
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

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED]))
					data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED] = data['alwaysenabled'];

				return data;
			},

			/**
			 * Static function to convert from model's object to legacy one
			 *
			 * @returns {Object}
			 */
			convertToLegacy: function (data) {
				return {
					active: data[CMDBuild.core.constants.Proxy.ACTIVE],
					alwaysenabled: data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED],
					forceFormat: data[CMDBuild.core.constants.Proxy.FORCE_FORMAT],
					id: data[CMDBuild.core.constants.Proxy.ID],
					label: data[CMDBuild.core.constants.Proxy.LABEL],
					preset: data[CMDBuild.core.constants.Proxy.PRESET],
					readOnlyAttributes: data[CMDBuild.core.constants.Proxy.READ_ONLY_ATTRIBUTES],
					reportCode: data[CMDBuild.core.constants.Proxy.REPORT_CODE],
					type: data[CMDBuild.core.constants.Proxy.TYPE]
				};
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
