(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.advanced.window.relations.DestinationEditorStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.TABLE_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' } // FIXME: waiting for refactor (renamed as description on server side)
		]
	});

})();