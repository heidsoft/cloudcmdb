(function() {

	Ext.define('CMDBuild.controller.common.field.picker.Color', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldPickerColorChange',
			'onFieldPickerColorIsValid',
			'onFieldPickerColorSelect',
			'onFieldPickerColorValueGet',
			'onFieldPickerColorValueSet'
		],

		/**
		 * @property {CMDBuild.view.common.field.picker.Color}
		 */
		view: undefined,

		/**
		 * @param {String} value
		 */
		onFieldPickerColorChange: function(value) {
			if (!Ext.isEmpty(value))
				return this.view.displayField.setBodyStyle({ 'background-color': value });

			return this.view.displayField.setBodyStyle({ 'background-color': '#FFFFFF' }); // Default color
		},

		/**
		 * Forward method
		 *
		 * @returns {Boolean}
		 */
		onFieldPickerColorIsValid: function() {
			return this.view.pickerField.isValid();
		},

		/**
		 * @param {String} selectedColor
		 */
		onFieldPickerColorSelect: function(selectedColor) {
			if (!Ext.isEmpty(selectedColor)) {
				this.cmfg('onFieldPickerColorValueSet', '#' + selectedColor);

				this.view.pickerField.picker.hide();
			}
		},

		/**
		 * Forward method
		 *
		 * @returns
		 */
		onFieldPickerColorValueGet: function() {
			return this.view.pickerField.getValue();
		},

		/**
		 * Forward method
		 *
		 * @param {String} value
		 */
		onFieldPickerColorValueSet: function(value) {
			return this.view.pickerField.setValue(value);
		}
	});

})();