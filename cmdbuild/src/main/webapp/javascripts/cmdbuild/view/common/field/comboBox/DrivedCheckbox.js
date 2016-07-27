(function() {

	Ext.define('CMDBuild.view.common.field.comboBox.DrivedCheckbox', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {CMDBuild.controller.common.field.comboBox.DrivedCheckbox}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,

		/**
		 * @cfg {Ext.data.Store}
		 */
		store: undefined,

		/**
		 * @cfg {String}
		 */
		valueField: CMDBuild.core.constants.Proxy.VALUE,

		considerAsFieldToDisable: true,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.comboBox.DrivedCheckbox', { view: this }),
				items: [
					this.diverCheckboxField = Ext.create('Ext.form.field.Checkbox', {
						disablePanelFunctions: true,
						flex: 1,

						listeners: {
							scope: this,
							change: function(field, newValue, oldValue, eOpts) {
								return this.delegate.cmfg('fieldComboboDrivedCheckboxCheckboxFieldChange');
							}
						}
					}),
					this.comboboxField = Ext.create('Ext.form.field.ComboBox', {
						disablePanelFunctions: true,
						displayField: this.displayField,
						valueField: this.valueField,
						flex: 7,
						forceSelection: true,
						editable: false,
						disabled: true,

						store: this.store,
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * Forward method
		 *
		 * @return {String}
		 */
		getRawValue: function() {
			return this.comboboxField.getRawValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Number}
		 */
		getValue: function() {
			return this.comboboxField.getValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Boolean}
		 */
		isValid: function() {
			return this.comboboxField.isValid();
		},

		/**
		 * @param {Mixed} value
		 */
		setValue: function(value) {
			this.delegate.cmfg('fieldComboboDrivedCheckboxValueSet', value);
		},

		/**
		 * Forward method
		 */
		reset: function() {
			this.comboboxField.reset();
		}
	});

})();