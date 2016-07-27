(function() {

	Ext.define('CMDBuild.controller.common.field.comboBox.DrivedCheckbox', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldComboboDrivedCheckboxCheckboxFieldChange',
			'fieldComboboDrivedCheckboxValueSet'
		],

		/**
		 * @property {CMDBuild.view.common.field.comboBox.DrivedCheckbox}
		 */
		view: undefined,

		fieldComboboDrivedCheckboxCheckboxFieldChange: function() {
			this.view.comboboxField.setDisabled(!this.view.diverCheckboxField.getValue());
		},

		/**
		 * @params {Mixed} value
		 */
		fieldComboboDrivedCheckboxValueSet: function(value) {
			if (!Ext.isEmpty(value)) {
				this.view.diverCheckboxField.setValue(true);
				this.view.comboboxField.setValue(value);
			} else {
				this.view.diverCheckboxField.setValue(false);
				this.view.comboboxField.setValue();
			}
		}
	});

})();
