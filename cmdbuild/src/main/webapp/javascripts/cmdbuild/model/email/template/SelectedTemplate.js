(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.email.template.SelectedTemplate', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.BCC, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.BODY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CC, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FROM, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TO, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VARIABLES, type: 'auto' }
		]
	});

})();
