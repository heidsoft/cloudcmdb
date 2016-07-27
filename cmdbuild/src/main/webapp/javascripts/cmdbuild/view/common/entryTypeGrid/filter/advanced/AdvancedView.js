(function () {

	Ext.define('CMDBuild.view.common.entryTypeGrid.filter.advanced.AdvancedView', {
		extend: 'Ext.container.ButtonGroup',

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Advanced}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.filter.SearchClear}
		 */
		clearButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.filter.SearchSet}
		 */
		manageToggleButton: undefined,

		border: false,
		frame: false,
		shadow: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.manageToggleButton = Ext.create('CMDBuild.core.buttons.iconized.filter.SearchSet', {
						enableToggle: true,
						scope: this,

						toggleHandler: function (button, state) {
							this.delegate.cmfg('onEntryTypeGridFilterAdvancedManageToggleButtonClick', state);
						}
					}),
					this.clearButton = Ext.create('CMDBuild.core.buttons.iconized.filter.SearchClear', {
						disabled: true,
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onEntryTypeGridFilterAdvancedClearButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * Override to avoid full button group container disabled
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		disable: function () {
			this.delegate.cmfg('onEntryTypeGridFilterAdvancedDisable');
		},

		/**
		 * @returns {Void}
		 *
		 * @deprecated
		 */
		disableClearFilterButton: function () {
			_deprecated('disableClearFilterButton', this);

			this.clearButton.disable();
		},

		/**
		 * Override to avoid full button group container disabled
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		enable: function () {
			this.delegate.cmfg('onEntryTypeGridFilterAdvancedEnable');
		},

		/**
		 * @returns {Void}
		 *
		 * @deprecated
		 */
		enableClearFilterButton: function () {
			_deprecated('enableClearFilterButton', this);

			this.clearButton.enable();
		},

		/**
		 * @param {String} label
		 *
		 * @returns {Ext.data.Store}
		 *
		 * @deprecated
		 */
		getFilterStore: function () {
			_deprecated('getFilterStore', this);

			return this.delegate.controllerManager.grid.getStore();
		},

		/**
		 * @param {String} label
		 *
		 * @returns {Void}
		 *
		 * @deprecated
		 */
		setFilterButtonLabel: function (label) {
			_deprecated('setFilterButtonLabel', this);

			this.delegate.cmfg('entryTypeGridFilterAdvancedManageToggleButtonLabelSet', label);
		}
	});

})();
