(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.localization.Localization', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TAG, type: 'string' }
		]
	});

})();
