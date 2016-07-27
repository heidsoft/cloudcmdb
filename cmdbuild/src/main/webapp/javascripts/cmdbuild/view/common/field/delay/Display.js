(function() {

	Ext.define('CMDBuild.view.common.field.delay.Display', {
		extend: 'Ext.form.field.Display',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {CMDBuild.controller.common.field.delay.Display}
		 */
		delegate: undefined,

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.controller.common.field.delay.Display', { view: this });

			this.callParent(arguments);
		},

		/**
		 * Forward method
		 *
		 * @param {Number} value
		 */
		setValue: function(value) {
			return this.callParent([this.delegate.cmfg('translateValue', value)]);
		}
	});

})();