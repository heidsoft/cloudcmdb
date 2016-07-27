(function () {

	Ext.define('CMDBuild.controller.administration.email.template.Values', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.template.Template}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEmailTemplateValuesWindowAbortButtonClick',
			'onEmailTemplateValuesWindowDeleteRowButtonClick',
			'onEmailTemplateValuesWindowSaveButtonClick'
		],

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.template.ValuesWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.email.template.Template} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.template.ValuesWindow', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;

			// Show window
			if (!Ext.isEmpty(this.view)) {
				this.storeDataSet();

				this.view.show();
			}
		},

		onEmailTemplateValuesWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * @param {CMDBuild.model.email.template.Variable} record
		 */
		onEmailTemplateValuesWindowDeleteRowButtonClick: function(record) {
			this.grid.getStore().remove(record);
		},

		onEmailTemplateValuesWindowSaveButtonClick: function() {
			this.cmfg('emailTemplateValuesDataSet', this.storeDataGet());

			this.onEmailTemplateValuesWindowAbortButtonClick();
		},

		/**
		 * @return {Object} data
		 *
		 * 	Example:
		 * 		{
		 * 			key1: value1,
		 * 			key2: value2,
		 * 			...
		 * 		}
		 *
		 * @private
		 */
		storeDataGet: function() {
			var data = {};

			// To validate and filter grid rows
			this.grid.getStore().each(function(record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.KEY))
					&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
				) {
					data[record.get(CMDBuild.core.constants.Proxy.KEY)] = record.get(CMDBuild.core.constants.Proxy.VALUE);
				}
			});

			return data;
		},

		/**
		 * Rewrite of loadData
		 *
		 * @private
		 */
		storeDataSet: function() {
			var data = this.cmfg('emailTemplateValuesDataGet');
			var store = this.grid.getStore();
			store.removeAll();

			if (!Ext.isEmpty(data))
				Ext.Object.each(data, function(key, value, myself) {
					var recordConf = {};
					recordConf[CMDBuild.core.constants.Proxy.KEY] = key;
					recordConf[CMDBuild.core.constants.Proxy.VALUE] = value || '';

					store.add(recordConf);
				}, this);
		}
	});

})();