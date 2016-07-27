(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.tabs.email.RegenerationEndPointCallback', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.FUNCTION, type: 'auto', defaultValue: Ext.emptyFn },
			{ name: CMDBuild.core.constants.Proxy.SCOPE, type: 'auto' }
		]
	});

})();
