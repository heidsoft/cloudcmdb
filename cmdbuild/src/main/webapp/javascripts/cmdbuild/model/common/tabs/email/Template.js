(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.tabs.email.Template', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.BCC, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.BODY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CC, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CONDITION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FROM, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.KEY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFY_WITH, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TO, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VARIABLES, type: 'auto' }
		],

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function (data) {
			if (!Ext.isEmpty(data) && !Ext.isEmpty(data[CMDBuild.core.constants.Proxy.ID]))
				delete data[CMDBuild.core.constants.Proxy.ID];

			this.callParent(arguments);
		},

		/**
		 * Removes ID from data array. This model hasn't ID property but in getData is returned as undefined. Probably a bug.
		 *
		 * @param {Boolean} includeAssociated
		 *
		 * @return {Object}
		 */
		getData: function (includeAssociated) {
			var data = this.callParent(arguments);

			delete data[CMDBuild.core.constants.Proxy.ID];

			return data;
		}
	});

})();
