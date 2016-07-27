(function() {

	/**
	 * NOTES
	 * - if no minValue/maxLabel are defined will be used minValue/maxValue as labels
	 */
	Ext.define('CMDBuild.view.common.field.slider.SingleWithExtremeLabels', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {String}
		 */
		maxLabel: '',

		/**
		 * @cfg {Number}
		 */
		maxValue: 100,

		/**
		 * @cfg {String}
		 */
		minLabel: '',

		/**
		 * @cfg {Number}
		 */
		minValue: 0,

		/**
		 * @property {Ext.slider.Single}
		 */
		sliderField: undefined,

		/**
		 * @cfg {Boolean}
		 */
		useTips: true,

		considerAsFieldToDisable: true,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.Display', {
						disablePanelFunctions: true,
						submitValue: false,
						value: Ext.isEmpty(this.minLabel) ? this.minValue : this.minLabel
					}),
					this.sliderField = Ext.create('Ext.slider.Single', {
						disablePanelFunctions: true,
						flex: 1,
						maxValue: this.maxValue,
						minValue: this.minValue,
						padding: '0 5',
						submitValue: false,
						useTips: this.useTips
					}),
					Ext.create('Ext.form.field.Display', {
						disablePanelFunctions: true,
						submitValue: false,
						value: Ext.isEmpty(this.maxLabel) ? this.maxValue : this.maxLabel
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
			return this.sliderField.getRawValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Number}
		 */
		getValue: function() {
			return this.sliderField.getValue();
		},

		/**
		 * Forward method
		 *
		 * @return {Boolean}
		 */
		isValid: function() {
			return this.sliderField.isValid();
		},

		/**
		 * Forward method
		 */
		reset: function() {
			this.sliderField.reset();
		},

		/**
		 * @param {Boolean} state
		 */
		setDisabled: function(state) {
			this.sliderField.setDisabled(state);
		},

		/**
		 * Forward method
		 *
		 * @param {Number} value
		 */
		setValue: function(value) {
			return this.sliderField.setValue(value);
		}
	});

})();