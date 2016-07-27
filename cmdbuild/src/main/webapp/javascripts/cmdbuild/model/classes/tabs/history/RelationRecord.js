(function() {

	Ext.require([
		'CMDBuild.core.configurations.DataFormat',
		'CMDBuild.core.constants.Proxy'
	]);

	Ext.define('CMDBuild.model.classes.tabs.history.RelationRecord', {
		extend: 'Ext.data.Model',

		idProperty: '', // HACK: avoids to use id field as record identifier, fixes a bug of duplicates rows id

		fields: [
			{ name: CMDBuild.core.constants.Proxy.BEGIN_DATE, type: 'date', dateFormat: CMDBuild.core.configurations.DataFormat.getDateTime() },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DOMAIN, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.END_DATE, type: 'date', dateFormat: CMDBuild.core.configurations.DataFormat.getDateTime() },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IS_CARD, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.IS_RELATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.USER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VALUES, type: 'auto' } // Historic relation values
		]
	});

})();