(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.menu.accordion.Management', {
		extend: 'Ext.data.TreeModel',

		fields: [
			{ name: 'cmIndex', type: 'int' },
			{ name: 'cmName', type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' }, // Text alias
			{ name: CMDBuild.core.constants.Proxy.ENTITY_ID, type: 'int', useNull: true }, // Menu item target entity id
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto' }, // Compatibility parameter with Class/Filter accordion
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SOURCE_FUNCTION, type: 'string' }, // Compatibility parameter with DataView accordion
			{ name: CMDBuild.core.constants.Proxy.SECTION_HIERARCHY, type: 'auto', defaultValue: [] }, // Service parameter used on multilevel accordions
			{ name: CMDBuild.core.constants.Proxy.SELECTABLE, type: 'boolean', defaultValue: true }, // Property to enable/disable node selection
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }
		]
	});

})();
