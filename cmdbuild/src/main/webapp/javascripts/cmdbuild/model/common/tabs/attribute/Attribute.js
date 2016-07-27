(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.common.tabs.attribute.Attribute', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'inherited', type: 'boolean' },
			{ name: 'isbasedsp', type: 'boolean' },
			{ name: 'isnotnull', type: 'boolean' },
			{ name: 'isunique', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ABSOLUTE_CLASS_ORDER, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_ORDER_SIGN, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.EDITOR_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FIELD_MODE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.GROUP, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.INDEX, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.META, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		]
	});

})();
