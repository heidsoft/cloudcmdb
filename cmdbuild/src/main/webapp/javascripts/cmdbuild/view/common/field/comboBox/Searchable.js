(function () {

	Ext.define('CMDBuild.view.common.field.comboBox.Searchable', {
		extend: 'Ext.form.field.ComboBox',

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.Searchable}
		 */
		delegate: undefined,

		/**
		 * @cfg {CMDBuild.model.common.attributes.Attribute}
		 */
		attributeModel: undefined,

		/**
		 * @cfg {Object}
		 */
		configuration: {},

		hideTrigger1: false,
		hideTrigger2: false,
		hideTrigger3: false,
		labelAlign: 'right',
		trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
		trigger3Cls: Ext.baseCSSPrefix + 'form-search-trigger',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.controller.common.field.comboBox.Searchable', { view: this });

			this.callParent(arguments);
		},

		/**
		 * Check if field returned value exists inside store
		 *
		 * @param {String} rawValue
		 *
		 * @returns {Array}
		 *
		 * @override
		 */
		getErrors: function (rawValue) {
			if (!Ext.isEmpty(rawValue) && this.getStore().find(this.valueField, this.getValue()) == -1)
				return [CMDBuild.Translation.errors.valueDoesNotMatchFilter];

			return this.callParent(arguments);
		},

		/**
		 * Compatibility with template resolver.
		 * Used by the template resolver to know if a field is a combo and to take the value of multilevel lookup
		 *
		 * @returns {String}
		 */
		getReadableValue: function () {
			return this.getRawValue();
		},

		/**
		 * Return value only if number, to avoid wrong and massive server requests from template resolver where returned value from field is a string
		 *
		 * @returns {Number}
		 *
		 * @override
		 */
		getValue: function () {
			var value = this.callParent(arguments);

			return Ext.isNumber(value) ? value : '';
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onKeyUp: function () {
			if (this.delegate.cmfg('fieldComboBoxSearchableStoreExceedsLimit')) {
				this.delegate.cmfg('onFieldComboBoxSearchableKeyUp');
			} else {
				this.callParent(arguments);
			}
		},

		/**
		 * @returns {Void}
		 */
		onTrigger1Click: function () {
			this.delegate.cmfg('onFieldComboBoxSearchableTrigger1Click');
		},

		/**
		 * @returns {Void}
		 */
		onTrigger2Click: function () {
			this.delegate.cmfg('onFieldComboBoxSearchableTrigger2Click');
		},

		/**
		 * @returns {Void}
		 */
		onTrigger3Click: function (value) {
			this.delegate.cmfg('onFieldComboBoxSearchableTrigger3Click');
		},

		/**
		 * Adds values in store if not already inside
		 *
		 * @param {String} value
		 *
		 * @returns {CMDBuild.view.common.field.comboBox.Searchable}
		 *
		 * @override
		 */
		setValue: function (value) {
			value = this.delegate.cmfg('fieldComboBoxSearchableNormalizeValue', value);

			if (Ext.isEmpty(value))
				return this.callParent();

			// Value in store
			if (this.getStore().find(this.valueField, value) >= 0)
				return this.callParent([value]);

			// Value not in store
			return this.delegate.cmfg('onFieldComboBoxSearchableValueSet', value);
		}
	});

})();
