(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.userAndGroup.group.UsersGrid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'userid',  type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string', mapping: 'userid' }, // TODO: waiting for refactor
			{ name: CMDBuild.core.constants.Proxy.USERNAME, type: 'string' }
		]
	});

})();
