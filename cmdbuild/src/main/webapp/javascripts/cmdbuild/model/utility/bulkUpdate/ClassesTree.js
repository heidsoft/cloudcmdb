(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.utility.bulkUpdate.ClassesTree', {
		extend: 'Ext.data.TreeModel',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' }, // Text alias
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SECTION_HIERARCHY, type: 'auto' }, // Service parameter used on multilevel accordions
			{ name: CMDBuild.core.constants.Proxy.SELECTABLE, type: 'boolean', defaultValue: true }, // Property to enable/disable node selection
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }
		]
	});

})();
