(function() {

	Ext.define('CMDBuild.view.common.field.delay.Delay', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {CMDBuild.controller.common.field.delay.Delay}
		 */
		delegate: undefined,

		considerAsFieldToDisable: true,

		// TODO: setup right width when i implement also advanced button

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.controller.common.field.delay.Delay', { view: this });

			this.delaySelectionCombo = Ext.create('Ext.form.field.ComboBox', {
				name: this.name, // Property forward
				value: this.valueFilter(this.value), // Property forward
				displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
				valueField: CMDBuild.core.constants.Proxy.VALUE,
				editable: false,
				allowBlank: true,
				flex: 2,

				listConfig: {
					getInnerTpl: function(displayField) { // Custom rendering template for each item
						return '<div class="x-combo-list-item">{description}&nbsp;<\/div>';
					}
				},

				store: this.delegate.cmfg('getDelayStore'),
				queryMode: 'local',

				listeners: {
					scope: this,
					beforeselect: function(combo, record, index, eOpts) {
						return this.delegate.cmfg('onDelayBeforeSelect', record);
					}
				}
			});

			Ext.apply(this, {
				items: [
					this.delaySelectionCombo
// TODO: Future implementation
//					,
//					Ext.create('Ext.button.Button', {
//						icon: 'images/icons/table.png',
//						margin: '0 0 0 3',
//						scope: this,
//
//						handler: function(button, e) {
//							_debug('ButtonClick');
//						}
//					})
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
			return this.delaySelectionCombo.getRawValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Number}
		 */
		getValue: function() {
			return this.delaySelectionCombo.getValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Boolean}
		 */
		isValid: function() {
			return this.delaySelectionCombo.isValid();
		},

		/**
		 * Forward method
		 *
		 * @param {Number} value
		 */
		setValue: function(value) {
			return this.delaySelectionCombo.setValue(this.valueFilter(value));
		},

		/**
		 * Forward method
		 */
		reset: function() {
			this.delaySelectionCombo.reset();
		},

		/**
		 * @param {Number} value
		 *
		 * @return {Number} or null
		 *
		 * @private
		 */
		valueFilter: function(value) {
			return (Ext.isNumber(value) && value > 0) ? value : null;
		}
	});

})();