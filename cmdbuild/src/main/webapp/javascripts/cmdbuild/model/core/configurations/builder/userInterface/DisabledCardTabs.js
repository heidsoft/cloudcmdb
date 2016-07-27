(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.core.configurations.builder.userInterface.DisabledCardTabs', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CLASS_ATTACHMENT_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_DETAIL_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_EMAIL_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_HISTORY_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NOTE_TAB, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_RELATION_TAB, type: 'boolean' }
		]
	});

})();
