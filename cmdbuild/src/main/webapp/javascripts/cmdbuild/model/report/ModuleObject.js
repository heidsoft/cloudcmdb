(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * To build module report object on management side
	 *
	 * @management
	 */
	Ext.define('CMDBuild.model.report.ModuleObject', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EXTENSION, type: 'string', defaultValue: CMDBuild.core.constants.Proxy.PDF },
			{ name: CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();