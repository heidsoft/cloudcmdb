(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.Advanced', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.Advanced}
		 */
		delegate: undefined,

		/**
		 * @cfg {Object}
		 */
		fieldConfiguration: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.filter.Clear}
		 */
		filterClearButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.filter.Set}
		 */
		filterSetButton: undefined,

		/**
		 * @property {Ext.form.field.Display}
		 */
		label: undefined,

		considerAsFieldToDisable: true,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.label = Ext.create('Ext.form.field.Display', { value: CMDBuild.Translation.notSet }),
					this.filterSetButton = Ext.create('CMDBuild.core.buttons.iconized.filter.Set', {
						border: false,
						tooltip: CMDBuild.Translation.setFilter,
						margin: '0 5',
						scope: this,

						handler: function(button, e) {
							this.delegate.cmfg('onFieldFilterAdvancedFilterSetButtonClick');
						}
					}),
					this.filterClearButton = Ext.create('CMDBuild.core.buttons.iconized.filter.Clear', {
						border: false,
						tooltip: CMDBuild.Translation.clearFilter,
						scope: this,

						handler: function(button, e) {
							this.delegate.cmfg('onFieldFilterAdvancedFilterClearButtonClick');
						}
					})
				]
			});

			// Delegate must be instantiated before fields creation because of button's state setup
			this.delegate = Ext.create('CMDBuild.controller.common.field.filter.advanced.Advanced', { view: this });

			this.callParent(arguments);
		},

		/**
		 * Returns filter configuration object
		 *
		 * @returns {Object}
		 */
		getValue: function() {
			return this.delegate.cmfg('fieldFilterAdvancedFilterGet', CMDBuild.core.constants.Proxy.CONFIGURATION);
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			return !this.delegate.cmfg('fieldFilterAdvancedFilterIsEmpty', CMDBuild.core.constants.Proxy.CONFIGURATION);
		},

		reset: function() {
			return this.delegate.cmfg('onFieldFilterAdvancedReset');
		},

		/**
		 * @param {Object} value - Filter configuration object
		 */
		setValue: function(value) {
			this.delegate.cmfg('onFieldFilterAdvancedSetValue', value);
		}
	});

})();