(function () {

	Ext.define('CMDBuild.controller.management.utility.exportCsv.ExportCsv', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.utility.ExportCsv'
		],

		/**
		 * @cfg {CMDBuild.controller.management.utility.Utility}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUtilityExportCsvExportButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.utility.exportCsv.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.management.utility.exportCsv.ExportCsvView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.utility.Utility} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.utility.exportCsv.ExportCsvView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @returns {Void}
		 */
		onUtilityExportCsvExportButtonClick: function () {
			if (this.validate(this.form))
				CMDBuild.proxy.utility.ExportCsv.download({ form: this.form.getForm() });
		}
	});

})();
