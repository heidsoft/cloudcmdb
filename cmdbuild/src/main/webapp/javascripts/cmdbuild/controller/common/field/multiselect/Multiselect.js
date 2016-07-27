(function() {

	Ext.define('CMDBuild.controller.common.field.multiselect.Multiselect', {
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
			'onFieldMultiselectAfterRender',
			'onFieldMultiselectGetStore',
			'onFieldMultiselectReset',
			'onFieldMultiselectSelectAll'
		],

		/**
		 * @property {CMDBuild.view.common.field.multiselect.Multiselect}
		 */
		view: undefined,

		/**
		 * @returns {Mixed}
		 */
		onFieldMultiselectAfterRender: function() {
			switch (this.view.defaultSelection) {
				case 'all': {
					return this.cmfg('onFieldMultiselectSelectAll');
				} break;

				case 'none':
					return null;

				default:
					return this.view.value = this.view.defaultSelection;
			}
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Ext.data.Store}
		 */
		onFieldMultiselectGetStore: function() {
			return this.view.boundList.getStore();
		},

		onFieldMultiselectReset: function() {
			this.view.setValue();
		},

		onFieldMultiselectSelectAll: function() {
			var arrayGroups = [];

			Ext.Array.forEach(this.view.getStore().getRange(), function(record, i, allRecords) {
				arrayGroups.push(record.get(this.view.valueField));
			}, this);

			this.view.setValue(arrayGroups);
		}
	});

})();