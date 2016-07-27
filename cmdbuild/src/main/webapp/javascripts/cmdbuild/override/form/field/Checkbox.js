(function () {

	Ext.define('CMDBuild.override.form.field.Checkbox', {
		override: 'Ext.form.field.Checkbox',

		/**
		 * To fix problem that don't set checkbox value using mixin value property - 17/02/2015
		 *
		 * @param {Object} config
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (config) {
			config = config || {};

			if (Ext.isBoolean(config.value))
				config.checked = config.value;

			this.callParent([config]);
		}
	});

})();
