(function () {

	Ext.define('CMDBuild.view.common.field.comboBox.Erasable', {
		extend: 'Ext.form.field.ComboBox',

		alias: 'widget.comboboxerasable',

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.Erasable}
		 */
		delegate: undefined,

		hideTrigger1: false,
		hideTrigger2: false,
		trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.comboBox.Erasable', { view: this })
			});

			this.callParent(arguments);
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
		 * @returns {Void}
		 */
		onTrigger1Click: function () {
			this.delegate.cmfg('onFieldComboBoxErasableTrigger1Click');
		},

		/**
		 * @returns {Void}
		 */
		onTrigger2Click: function () {
			this.delegate.cmfg('onFieldComboBoxErasableTrigger2Click');
		}
	});

})();
