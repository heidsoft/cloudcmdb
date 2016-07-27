(function() {

	Ext.require([
		'CMDBuild.core.configurations.DataFormat',
		'CMDBuild.core.constants.Proxy'
	]);

	Ext.define('CMDBuild.model.classes.tabs.history.CardRecord', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.BEGIN_DATE, type: 'date', dateFormat: CMDBuild.core.configurations.DataFormat.getDateTime() },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.END_DATE, type: 'date', dateFormat: CMDBuild.core.configurations.DataFormat.getDateTime() },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IS_CARD, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.IS_RELATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.USER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VALUES, type: 'auto' } // Historic card values
		]
	});

})();