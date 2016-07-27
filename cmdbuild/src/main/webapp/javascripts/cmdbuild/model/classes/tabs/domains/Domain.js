(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename + setup correct types)
	 *
	 * @administration
	 */
	Ext.define('CMDBuild.model.classes.tabs.domains.Domain', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'class1', type: 'string' },
			{ name: 'class2',type: 'string' },
			{ name: 'descrdir',type: 'string' },
			{ name: 'descrinv',type: 'string' },
			{ name: 'md',type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CARDINALITY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION,type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID_DOMAIN, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' }
		]
	});

})();
