(function () {

	Ext.define('CMDBuild.controller.management.utility.importCsv.ImportCsv', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.utility.ImportCsv'
		],

		/**
		 * @cfg {CMDBuild.controller.management.utility.Utility}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUtilityImportCsvAbortButtonClick',
			'onUtilityImportCsvClassSelected',
			'onUtilityImportCsvConfirmButtonClick',
			'onUtilityImportCsvUpdateButtonClick',
			'onUtilityImportCsvUploadButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.utility.importCsv.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.importCsv.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {CMDBuild.view.management.utility.importCsv.ImportCsvView}
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

			this.view = Ext.create('CMDBuild.view.management.utility.importCsv.ImportCsvView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		gridRefresh: function () {
			CMDBuild.proxy.utility.ImportCsv.getStoreRecords({
				scope: this,
				success: function (response, options, decodedResponse) {
					this.grid.configureHeadersAndStore(decodedResponse[CMDBuild.core.constants.Proxy.HEADERS]);
					this.grid.loadData(decodedResponse[CMDBuild.core.constants.Proxy.ROWS]);
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onUtilityImportCsvAbortButtonClick: function () {
			this.form.reset();

			this.grid.removeAll();
		},

		/**
		 * @param {CMDBuild.model.utility.importCsv.Class} selectedRecord
		 *
		 * @returns {Void}
		 */
		onUtilityImportCsvClassSelected: function (selectedRecord) {
			if (Ext.isObject(selectedRecord) && !Ext.Object.isEmpty(selectedRecord)) {
				this.grid.updateStoreForClassId(selectedRecord.get(CMDBuild.core.constants.Proxy.ID));
			} else {
				_error('wrong or empty selectedRecord parameter', this);
			}
		},

		/**
		 * @returns {Void}
		 */
		onUtilityImportCsvConfirmButtonClick: function () {
			CMDBuild.proxy.utility.ImportCsv.update({
				scope: this,
				failure: function (response, options, decodedResponse) {
					CMDBuild.core.Message.error(CMDBuild.Translation.error, CMDBuild.Translation.importFailed, true);
				},
				success: function (response, options, decodedResponse) {
					CMDBuild.core.Message.info(CMDBuild.Translation.info, CMDBuild.Translation.importWasSuccessful);

					this.gridRefresh();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onUtilityImportCsvUpdateButtonClick: function () {
			var records = this.grid.getRecordToUpload();

			if (!Ext.isEmpty(records) && Ext.isArray(records)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.DATA] = Ext.encode(records);

				CMDBuild.proxy.utility.ImportCsv.updateRecords({
					params: params,
					scope: this,
					success: this.gridRefresh
				});
			} else {
				CMDBuild.core.Message.warning(CMDBuild.Translation.warning, CMDBuild.Translation.noCardsToUpdate);
			}
		},

		/**
		 * @returns {Void}
		 */
		onUtilityImportCsvUploadButtonClick: function () {
			if (this.validate(this.form))
				CMDBuild.proxy.utility.ImportCsv.upload({
					form: this.view.form.getForm(),
					scope: this,
					success: this.gridRefresh
				});
		}
	});

})();
