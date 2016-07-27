(function() {

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.lookup.GridStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Active', type: 'boolean', defaultValue: true },
			{ name: 'Code', type: 'string' },
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', useNull: true },
			{ name: 'Notes', type: 'string' },
			{ name: 'Number', type: 'int' },
			{ name: 'ParentDescription', type: 'string' },
			{ name: 'ParentId', type: 'int', useNull: true },
			{ name: 'TranslationUuid', type: 'string' }
		]
	});

})();