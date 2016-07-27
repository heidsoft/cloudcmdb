(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.Classes.domainsTreePanel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' }
		]
	});

})();