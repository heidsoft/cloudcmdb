(function() {

	Ext.define('CMDBuild.view.common.field.multiselect.Group', {
		extend: 'Ext.ux.form.MultiSelect',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.multiselect.Group'
		],

		/**
		 * @property {CMDBuild.controller.common.field.multiselect.Group}
		 */
		delegate: undefined,

		allowBlank: true,
		considerAsFieldToDisable: true,
		displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
		fieldLabel: CMDBuild.Translation.enabledGroups,
		name: CMDBuild.core.constants.Proxy.GROUPS,
		valueField: CMDBuild.core.constants.Proxy.ID,

		initComponent: function () {
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.multiselect.Group', { view: this }),
				store: CMDBuild.proxy.common.field.multiselect.Group.getStore()
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return this.delegate.cmfg('onFieldMultiselectGroupGetStore');
		},

		reset: function () {
			this.delegate.cmfg('onFieldMultiselectGroupReset');
		},

		selectAll: function () {
			this.delegate.cmfg('onFieldMultiselectGroupSelectAll');
		},

		/**
		 * Overrides to avoid multiselect native behaviour that sets the field as readonly if disabled
		 *
		 * @override
		 */
		updateReadOnly: Ext.emptyFn
	});

})();
