(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.widget.workflow.PresetGrid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.VALUE, type: 'string' }
		]
	});

})();