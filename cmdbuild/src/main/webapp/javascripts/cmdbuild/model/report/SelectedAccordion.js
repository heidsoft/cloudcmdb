(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @management
	 */
	Ext.define('CMDBuild.model.report.SelectedAccordion', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SECTION_HIERARCHY, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		]
	});

})();