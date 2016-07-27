(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.userAndGroup.user.DefaultGroup', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'isdefault', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true }
		]
	});

})();
