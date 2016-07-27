(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.tabs.email.Email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ATTACHMENTS, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.BCC, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.BODY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CC, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.DATE, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.FROM, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.KEY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFY_WITH, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.REFERENCE, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.STATUS, type: 'string', defaultValue: CMDBuild.core.constants.Proxy.DRAFT },
			{ name: CMDBuild.core.constants.Proxy.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEMPORARY, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.TO, type: 'auto' }
		],

		/**
		 * Converts model object to params object, used in server calls
		 *
		 * @param {Array} requiredAttributes
		 *
		 * @return {Object} params
		 */
		getAsParams: function (requiredAttributes) {
			var params = {};

			// With no parameters returns all data
			if (Ext.isEmpty(requiredAttributes)) {
				params = this.getData();
			} else {
				// Or returns only required attributes
				Ext.Array.forEach(requiredAttributes, function (item, index, allItems) {
					if (item == CMDBuild.core.constants.Proxy.TEMPLATE) { // Support for template objects
						params[CMDBuild.core.constants.Proxy.TEMPLATE] =
							this.get(CMDBuild.core.constants.Proxy.TEMPLATE)[CMDBuild.core.constants.Proxy.NAME]
						|| this.get(CMDBuild.core.constants.Proxy.TEMPLATE);
					} else {
						params[item] = this.get(item) || null;
					}
				}, this);
			}

			return params;
		}
	});

})();
