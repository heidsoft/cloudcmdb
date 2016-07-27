(function() {

	Ext.require(['CMDBuild.core.constants.Proxy']);

	Ext.define('CMDBuild.model.widget.customForm.Capabilities', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ADD_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLONE_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DELETE_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.EXPORT_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IMPORT_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.MODIFY_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REFRESH_BEHAVIOUR, type: 'string', defaultValue: 'everyTime' } // Configuration to indicate when recalculate content from function [everyTime || firstTime]
		]
	});

})();