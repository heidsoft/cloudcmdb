(function() {

	/**
	 * @deprecated (CMDBuild.view.common.field.multiselect.Group)
	 */
	Ext.define('CMDBuild.view.common.field.CMGroupSelectionList', { // asd
		extend: 'Ext.ux.form.MultiSelect',

		requires: ['CMDBuild.proxy.common.field.multiselect.Group'],

		considerAsFieldToDisable: true,

		fieldLabel: CMDBuild.Translation.enabledGroups,
		name: CMDBuild.core.constants.Proxy.GROUPS,
		dataFields: [
			CMDBuild.core.constants.Proxy.NAME,
			CMDBuild.core.constants.Proxy.ID,
			CMDBuild.core.constants.Proxy.DESCRIPTION
		],
		valueField: CMDBuild.core.constants.Proxy.ID,
		displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
		allowBlank: true,

		initComponent: function() {
			Ext.applyIf(this, {
				store: CMDBuild.proxy.common.field.multiselect.Group.getStore()
			});

			this.callParent(arguments);
		},

		// The origianl multiselect set the field as readonly if disabled.
		// We don't want this behabiour.
		updateReadOnly: Ext.emptyFn,

		reset: function() {
			this.setValue([]);
		},

		selectAll: function() {
			var arrayGroups = [];

			this.store.data.each(function(item, index, totalItems) {
				arrayGroups.push(item.data.name);
			});

			this.setValue(arrayGroups);
		}
	});

})();