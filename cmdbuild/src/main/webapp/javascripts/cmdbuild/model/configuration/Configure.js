(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * Runtime configure model used in main CMDBuild configuration procedure
	 */
	Ext.define('CMDBuild.model.configuration.Configure', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.JDBC_DRIVER_VERSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LANGUAGE, type: 'string', defaultValue: 'en' }
		]
	});

})();
