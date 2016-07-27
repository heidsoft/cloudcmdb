(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.grid.ImportCSV', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.grid.Csv'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onImportCSVAbortButtonClick',
			'onImportCSVUploadButtonClick'
		],

		/**
		 * @cfg {Number}
		 */
		classId: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.ImportCSVWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.widgets.grid.Grid} configurationObject.parentDelegate
		 * @param {Number} configurationObject.classId
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.grid.ImportCSVWindow', {
				delegate: this
			});

			this.view.classIdField.setValue(this.classId);

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onImportCSVAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onImportCSVUploadButtonClick: function() {
			CMDBuild.core.LoadMask.show();
			CMDBuild.proxy.grid.Csv.upload({
				form: this.view.csvUploadForm.getForm(),
				scope: this,
				success: function(form, action) {
					CMDBuild.proxy.grid.Csv.getRecords({
						scope: this,
						success: function(result, options, decodedResult) {
							this.cmfg('setGridDataFromCsv', {
								rawData: decodedResult.rows,
								mode: this.view.csvImportModeCombo.getValue()
							});

							this.onImportCSVAbortButtonClick();
						}
					});
				},
				failure: function(form, action) {
					CMDBuild.core.LoadMask.hide();

					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
						false
					);
				}
			});
		}
	});

})();