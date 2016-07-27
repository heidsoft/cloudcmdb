(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.tabs.email.SelectedEntity', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ENTITY, type: 'auto' }, // Class or Activity object
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true }
		]
	});

})();
