(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.report.Grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.GROUPS, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.QUERY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TITLE, type: 'string' }, // TODO: waiting for refactor (rename)
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();