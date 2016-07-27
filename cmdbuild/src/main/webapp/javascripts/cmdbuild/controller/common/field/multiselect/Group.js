(function() {

	Ext.define('CMDBuild.controller.common.field.multiselect.Group', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldMultiselectGroupGetStore',
			'onFieldMultiselectGroupReset',
			'onFieldMultiselectGroupSelectAll'
		],

		/**
		 * @property {CMDBuild.view.common.field.multiselect.Group}
		 */
		view: undefined,

		/**
		 * Forwarder method
		 *
		 * @returns {Ext.data.Store}
		 */
		onFieldMultiselectGroupGetStore: function () {
			return this.view.boundList.getStore();
		},

		onFieldMultiselectGroupReset: function () {
			this.view.setValue();
		},

		onFieldMultiselectGroupSelectAll: function () {
			var arrayGroups = [];

			Ext.Array.forEach(this.view.getStore().getRange(), function (record, i, allRecords) {
				arrayGroups.push(record.get(CMDBuild.core.constants.Proxy.NAME));
			}, this);

			this.view.setValue(arrayGroups);
		}
	});

})();
