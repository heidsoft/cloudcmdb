(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * Build grid model from class attributes
	 */
	Ext.define('CMDBuild.model.widget.grid.Grid', {
		extend: 'Ext.data.Model',

		fields: [],

		/**
		 * @param {Array} fieldsDefinitions
		 */
		constructor: function (fieldsDefinitions) {
			var fieldsForModel = [];

			if (Ext.isArray(fieldsDefinitions)) {
				Ext.Array.forEach(fieldsDefinitions, function(field, i, allFields) {
					switch (field[CMDBuild.core.constants.Proxy.TYPE]) {
						case 'BOOLEAN': {
							fieldsForModel.push({ name: field[CMDBuild.core.constants.Proxy.NAME], type: 'boolean' });
						} break;

						case 'DATE': {
							fieldsForModel.push({ name: field[CMDBuild.core.constants.Proxy.NAME], type: 'date' });
						} break;

						case 'DECIMAL':
						case 'DOUBLE': {
							fieldsForModel.push({ name: field[CMDBuild.core.constants.Proxy.NAME], type: 'float', useNull: true });
						} break;

						case 'INTEGER': {
							fieldsForModel.push({ name: field[CMDBuild.core.constants.Proxy.NAME], type: 'int', useNull: true });
						} break;

						default: {
							fieldsForModel.push({ name: field[CMDBuild.core.constants.Proxy.NAME], type: 'string' });
						}
					}
				}, this);

				CMDBuild.model.widget.grid.Grid.setFields(fieldsForModel);
			}

			this.callParent();
		}
	});

})();
