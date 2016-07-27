(function() {

	Ext.define('CMDBuild.view.common.field.picker.Color', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {CMDBuild.controller.common.field.picker.Color}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		displayField: undefined,

		/**
		 * @property {Ext.form.field.Picker}
		 */
		pickerField: undefined,

		layout: {
			type: 'hbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.picker.Color', { view: this }),

				items: [
					this.pickerField = Ext.create('Ext.form.field.Picker', {
						disablePanelFunctions: true,
						submitValue: false,
						flex: 1,

						picker: Ext.create('Ext.picker.Color', {
							colors: [
								'000000', '444444', '666666', '999999', 'CCCCCC', 'EEEEEE', 'F3F3F3', 'FFFFFF',
								'FF0000', 'FF9900', 'FFFF00', '00FF00', '00FFFF', '0000FF', '9900FF', 'FF00FF',
								'F4CCCC', 'FCE5CD', 'FFF2CC', 'D9EAD3', 'D0E0E3', 'CFE2F3', 'D9D2E9', 'EAD1DC',
								'EA9999', 'F9CB9C', 'FFE599', 'B6D7A8', 'A2C4C9', '9FC5E8', 'B4A7D6', 'D5A6BD',
								'E06666', 'F6B26B', 'FFD966', '93C47D', '76A5AF', '6FA8DC', '8E7CC3', 'C27BA0',
								'CC0000', 'E69138', 'F1C232', '6AA84F', '45818E', '3D85C6', '674EA7', 'A64D79',
								'990000', 'B45F06', 'BF9000', '38761D', '134F5C', '0B5394', '351C75', '741B47',
								'660000', '783F04', '7F6000', '274E13', '0C343D', '073763', '20124D', '4C1130'
							],
							resizable: false,
							floating: true,
							minWidth: 144,
							minHeight: 144,
							scope: this,

							listeners: {
								scope: this,
								select: function(field, color, eOpts) {
									this.delegate.cmfg('onFieldPickerColorSelect', color);
								}
							}
						}),

						listeners: {
							scope: this,
							change: function(field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onFieldPickerColorChange', newValue);
							}
						}
					}),
					this.displayField = Ext.create('Ext.panel.Panel', {
						border: true,
						frame: false,
						margin: '0 0 0 5',
						width: 22 // Same as height to be a square
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {String}
		 */
		getValue: function() {
			return this.delegate.cmfg('onFieldPickerColorValueGet');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			return this.delegate.cmfg('onFieldPickerColorIsValid');
		},

		/**
		 * @param {String} value
		 */
		setValue: function(value) {
			return this.delegate.cmfg('onFieldPickerColorValueSet', value);
		}
	});

})();