(function () {

	/**
	 * Generic not tipized store
	 */
	Ext.define('CMDBuild.model.common.Generic', {
		extend: 'Ext.data.Model',

		fields:[],

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function(data) {
			data = data || {};

			CMDBuild.model.common.Generic.setFields(Ext.Object.getKeys(data));

			this.callParent(arguments);
		}
	});

})();
