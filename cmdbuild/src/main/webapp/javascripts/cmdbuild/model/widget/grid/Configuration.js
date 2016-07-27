(function () {

	Ext.require(['CMDBuild.core.constants.Proxy']);

	Ext.define('CMDBuild.model.widget.grid.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: 'outputName', type: 'string' },
			{ name: 'serializationType', type: 'string' },
			{ name: 'writeOnAdvance', type: 'booloean' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTE_SEPARATOR, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CARD_SEPARATOR, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DISABLE_ADD_ROW, type: 'boolean' }, // TODO: should be inside capabilities model
			{ name: CMDBuild.core.constants.Proxy.DISABLE_DELETE_ROW, type: 'boolean' }, // TODO: should be inside capabilities model
			{ name: CMDBuild.core.constants.Proxy.DISABLE_IMPORT_FROM_CSV, type: 'boolean' }, // TODO: should be inside capabilities model
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.KEY_VALUE_SEPARATOR, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PRESETS, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.PRESETS_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REQUIRED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VARIABLES, type: 'auto' } // Unmanaged variables
		]
	});

})();
