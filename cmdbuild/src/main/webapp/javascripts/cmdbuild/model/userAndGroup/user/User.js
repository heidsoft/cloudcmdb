(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.userAndGroup.user.User', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'defaultgroup', type: 'int', useNull: true },
			{ name: 'userid', type: 'int', useNull: true },
			{ name: 'username', type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.IS_ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.PRIVILEGED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.SERVICE, type: 'boolean', defaultValue: false }
		]
	});

})();
