(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.tabs.email.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.REQUIRED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATES, type: 'auto', defaultValue: [] }
		]
	});

})();
