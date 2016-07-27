(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.tabs.attribute.OrderGridRecord', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.CLASS_ORDER_SIGN, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' }
		]
	});

})();
