(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.email.account.SelectedAccount', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IMAP_PORT, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IMAP_SERVER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.IMAP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IMAP_START_TLS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.OUTPUT_FOLDER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SMTP_PORT, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.SMTP_SERVER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SMTP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.SMTP_START_TLS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.USERNAME, type: 'string' }
		]
	});

})();
