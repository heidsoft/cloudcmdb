(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @administration
	 *
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.widget.createModifyCard.Definition', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TARGET_CLASS, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: '.CreateModifyCard' }
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

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED]))
					data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED] = data['alwaysenabled'];

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.FILTER]))
					data[CMDBuild.core.constants.Proxy.FILTER] = data['idcardcqlselector'];

				if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.READ_ONLY]))
					data[CMDBuild.core.constants.Proxy.READ_ONLY] = data['readonly'];

				return data;
			},

			/**
			 * Static function to convert from model's object to legacy one
			 *
			 * @returns {Object}
			 */
			convertToLegacy: function(data) {
				return {
					active: data[CMDBuild.core.constants.Proxy.ACTIVE],
					alwaysenabled: data[CMDBuild.core.constants.Proxy.ALWAYS_ENABLED],
					id: data[CMDBuild.core.constants.Proxy.ID],
					idcardcqlselector: data[CMDBuild.core.constants.Proxy.FILTER],
					label: data[CMDBuild.core.constants.Proxy.LABEL],
					readonly: data[CMDBuild.core.constants.Proxy.READ_ONLY],
					targetClass: data[CMDBuild.core.constants.Proxy.TARGET_CLASS],
					type: data[CMDBuild.core.constants.Proxy.TYPE]
				};
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function(data) {
			data = CMDBuild.model.widget.createModifyCard.Definition.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();