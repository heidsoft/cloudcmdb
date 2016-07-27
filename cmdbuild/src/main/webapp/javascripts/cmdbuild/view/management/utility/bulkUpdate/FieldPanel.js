(function () {

	Ext.define('CMDBuild.view.management.utility.bulkUpdate.FieldPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		checkbox: undefined,

		/**
		 * @cfg {Object}
		 */
		field: undefined,

		border: false,
		frame: true,
		margin: '0 5 5 0',
		padding: '4 0 4 6',

		layout: {
			type: 'hbox',
			align: 'center'
		},

		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.checkbox = Ext.create('Ext.form.field.Checkbox', {
						name: (this.field.hiddenName || this.field.name) + '_check', // For combo send the hiddenName
						labelSeparator: '',
						uncheckedValue: 'false',
						inputValue: 'true',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.field.setDisabled(!newValue);

								if (newValue)
									this.field.focus(true);
							}
						}
					}),
					this.field
				]
			});

			this.callParent(arguments);
		}
	});

})();
