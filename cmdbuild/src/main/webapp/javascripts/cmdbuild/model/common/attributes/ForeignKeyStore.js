(function() {

	Ext.define('CMDBuild.model.common.attributes.ForeignKeyStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', useNull: true }
		]
	});

})();