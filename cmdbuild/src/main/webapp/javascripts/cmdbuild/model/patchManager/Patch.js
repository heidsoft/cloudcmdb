(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.patchManager.Patch', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CATEGORY,  type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME,  type: 'string' }
		]
	});

})();