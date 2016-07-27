(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.utility.importCsv.Class', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID,  type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEXT,  type: 'string' }, // TODO: waiting for refactor (rename description)
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		]
	});

})();