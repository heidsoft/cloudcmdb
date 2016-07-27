(function() {

	Ext.require(['CMDBuild.core.constants.Global']);

	Ext.define('CMDBuild.controller.management.workflow.StaticsController', {

		singleton: true,

		/**
		 * Iterates all process attributes and take only ones defined as variables for this step
		 *
		 * @param {Array} attributes
		 * @param {Array} variables
		 * 		Structure: {
		 * 			{String} name
		 * 			{Boolean} mandatory
		 * 			{Boolean} writable
		 * 		}
		 *
		 * @returns {Array} out
		 */
		filterAttributesInStep: function(attributes, variables) {
			var out = [];

			if (
				!Ext.isEmpty(attributes) && Ext.isArray(attributes)
				&& !Ext.isEmpty(variables) && Ext.isArray(variables)
			) {
				Ext.Array.each(variables, function(variableObject, i, allVariableObjects) {
					Ext.Array.each(attributes, function(attributeObject, i, allAttributeObjects) {
						if (attributeObject['name'] == variableObject['name']) {
							attributeObject['isnotnull'] = variableObject['mandatory'];
							attributeObject['fieldmode'] = variableObject['writable'] ? 'write' : 'read';

							out.push(attributeObject);

							return false;
						}
					}, this);
				}, this);
			}

			return out;
		},

		/**
		 * @param {Ext.form.Basic} form
		 *
		 * @returns {String or null} out
		 */
		getInvalidAttributeAsHTML: function(form) {
			var invalidFields = CMDBuild.controller.management.workflow.StaticsController.getInvalidField(form);
			var out = null;

			if (!Ext.Object.isEmpty(invalidFields) && Ext.isObject(invalidFields)) {
				out = '';

				Ext.Object.each(invalidFields, function(name, field, myself) {
					if (!Ext.isEmpty(field)) {
						var fieldLabel = field.getFieldLabel();

						// Strip label required flag
						if (fieldLabel.indexOf(CMDBuild.core.constants.Global.getMandatoryLabelFlag()) == 0)
							fieldLabel = fieldLabel.replace(CMDBuild.core.constants.Global.getMandatoryLabelFlag(), '');

						out += '<li>' + fieldLabel + '</li>';
					}
				}, this);

				out = '<ul>' + out + '</ul>';
			}

			return out;
		},

		/**
		 * Validates all fields (display panel fields and edit panel fields). To be globally valid is required just one of them
		 *
		 * @param {Ext.form.Basic} form
		 *
		 * @returns {Object} invalidFieldsMap
		 *
		 * @private
		 */
		getInvalidField: function(form) {
			var fieldsArray = form.getFields().getRange();
			var invalidFieldsMap = {};

			if (!Ext.isEmpty(fieldsArray) && Ext.isArray(fieldsArray))
				Ext.Array.each(fieldsArray, function(field, i, allFields) {
					if (!field.isValid()) {
						invalidFieldsMap[field.name] = field;
					} else if (!Ext.isEmpty(invalidFieldsMap[field.name])) {
						delete invalidFieldsMap[field.name];
					}
				}, this);

			return invalidFieldsMap;
		}
	});

})();