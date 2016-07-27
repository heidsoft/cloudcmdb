(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.cql.Cql', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CONTEXT, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.EXPRESSION, type: 'string' }
		]
	});

})();