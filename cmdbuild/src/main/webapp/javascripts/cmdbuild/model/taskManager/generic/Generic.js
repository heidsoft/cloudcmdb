(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.taskManager.generic.Generic', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },

			{ name: CMDBuild.core.constants.Proxy.CONTEXT, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL_TEMPLATE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REPORT_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REPORT_EXTENSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REPORT_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REPORT_PARAMETERS, type: 'auto', defaultValue: {} }
		]
	});

})();
