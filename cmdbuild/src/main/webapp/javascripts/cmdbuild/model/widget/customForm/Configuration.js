(function() {

	Ext.require(['CMDBuild.core.constants.Proxy']);

	Ext.define('CMDBuild.model.widget.customForm.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CAPABILITIES, type: 'auto' }, // Object to gather all UI disable flags
			{ name: CMDBuild.core.constants.Proxy.DATA, type: 'auto' }, // Encoded array of CMDBuild.model.common.attributes.Generic models strings
			{ name: CMDBuild.core.constants.Proxy.FUNCTION_DATA, type: 'auto' }, // Function data to be resolved with TemplateResolver (data attribute alias)
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LAYOUT, type: 'string', defaultValue: 'grid' }, // Widget view mode [grid|form]
			{ name: CMDBuild.core.constants.Proxy.MODEL, type: 'auto' }, // Encoded array of CMDBuild.model.widget.customForm.Attribute models strings
			{ name: CMDBuild.core.constants.Proxy.REQUIRED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VARIABLES, type: 'auto' } // Unmanaged variables
		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// Apply form model attributes model
			this.set(CMDBuild.core.constants.Proxy.MODEL, data[CMDBuild.core.constants.Proxy.MODEL]);

			// Decode data string
			this.set(CMDBuild.core.constants.Proxy.DATA, data[CMDBuild.core.constants.Proxy.DATA]);

			// Apply capabilities model
			this.set(CMDBuild.core.constants.Proxy.CAPABILITIES, data[CMDBuild.core.constants.Proxy.CAPABILITIES]);
		},

		/**
		 * @param {String} fieldName
		 * @param {Object} newValue
		 *
		 * @returns {String}
		 *
		 * @override
		 */
		set: function(fieldName, newValue) {
			if (!Ext.isEmpty(newValue)) {
				switch (fieldName) {
					case CMDBuild.core.constants.Proxy.CAPABILITIES: {
						newValue = Ext.create('CMDBuild.model.widget.customForm.Capabilities', newValue);
					} break;

					case CMDBuild.core.constants.Proxy.DATA: {
						newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

						var attributesArray = [];
						var attributesDataTypes = {};

						// Build attributes dataTypes
						Ext.Array.forEach(this.get(CMDBuild.core.constants.Proxy.MODEL), function(fieldModel, i, allfieldModels) {
							attributesDataTypes[fieldModel.get(CMDBuild.core.constants.Proxy.NAME)] = fieldModel.get(CMDBuild.core.constants.Proxy.TYPE);
						}, this);

						Ext.Array.forEach(newValue, function(attributeObject, i, allAttributeObjects) {
							attributesArray.push(Ext.create('CMDBuild.model.common.attributes.Generic', {
								data: attributeObject,
								dataTypes: attributesDataTypes
							}));
						}, this);

						newValue = attributesArray;
					} break;

					/**
					 * Uses custom Attribute model to adapt to old FieldManager implementation
					 * TODO: delete on full FieldManager implementation
					 */
					case CMDBuild.core.constants.Proxy.MODEL: {
						newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

						var attributesArray = [];

						Ext.Array.forEach(newValue, function(attributeObject, i, allAttributeObjects) {
							attributesArray.push(Ext.create('CMDBuild.model.widget.customForm.Attribute', attributeObject));
						}, this);

						newValue = attributesArray;
					} break;

					default: {
						if (Ext.isString(newValue))
							newValue = Ext.decode(newValue);
					}
				}
			}

			this.callParent(arguments);
		}
	});

})();