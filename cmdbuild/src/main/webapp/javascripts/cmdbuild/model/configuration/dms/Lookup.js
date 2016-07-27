(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.configuration.dms.Lookup', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' } // TODO: waiting for refactor (rename)
		]
	});

})();
