(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.filter.group.SelectedFilter', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CONFIGURATION, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_FOR_GROUPS, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ENTRY_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATE, type: 'boolean', defaultValue: true } // Client managed filters are true
		]
	});

})();