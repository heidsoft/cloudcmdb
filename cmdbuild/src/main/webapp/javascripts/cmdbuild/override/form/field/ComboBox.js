(function () {

	Ext.define('CMDBuild.override.form.field.ComboBox', {
		override: 'Ext.form.field.ComboBox',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.callParent(arguments);

			// Force store load on enable to avoid store's data incongruences - 01/04/2016
			this.on('enable', function (field, eOpts) {
				if (Ext.isBoolean(field.getStore().autoLoad) && field.getStore().autoLoad && !field.getStore().isLoading())
					field.getStore().load();
			}, this);
		},

		/**
		 * To fix problem that don't set combo value if forceSelection is true - 21/05/2014
		 *
		 * @param {Mixed} value
		 * @param {Boolean} doSelect
		 *
		 * @returns {Ext.form.field.ComboBox}
		 *
		 * @override
		 */
		setValue: function (value, doSelect) {
			var comboboxField = this;
			var forceSelectionState = this.forceSelection;

			if (forceSelectionState)
				this.forceSelection = false;

			if (!Ext.isEmpty(this.getStore()))
				comboboxField = this.callParent(arguments);

			if (forceSelectionState)
				this.forceSelection = true;

			return comboboxField;
		}
	});

})();
