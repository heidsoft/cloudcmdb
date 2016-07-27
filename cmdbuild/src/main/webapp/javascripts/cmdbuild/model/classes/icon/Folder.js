(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.classes.icon.Folder', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: '_id', type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'string' }
		]
	});

})();
