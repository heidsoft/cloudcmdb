(function () {

	Ext.define('CMDBuild.controller.administration.localization.ImportExport', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.localization.Export',
			'CMDBuild.proxy.localization.Import'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Localization}
		 */
		parentDelegate: undefined,

		/**
		 * Sections where activeOnly is managed on server side
		 *
		 * @cfg {Array}
		 */
		activeOnlySections: [
			CMDBuild.core.constants.Proxy.CLASS,
			CMDBuild.core.constants.Proxy.DOMAIN,
			CMDBuild.core.constants.Proxy.LOOKUP,
			CMDBuild.core.constants.Proxy.PROCESS
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationImportExportExportButtonClick',
			'onLocalizationImportExportExportSectionChange',
			'onLocalizationImportExportExportShow',
			'onLocalizationImportExportImportButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ExportForm}
		 */
		exportPanel: undefined,

		/**
		 * @property {CMDBuild.core.window.AbstractModal}
		 */
		failuresWindow: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ImportForm}
		 */
		importPanel: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.importExport.ImportExportView}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.Localization} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.importExport.ImportExportView', { delegate: this });

			// Shorthands
			this.exportPanel = this.view.exportPanel;
			this.importPanel = this.view.importPanel;
		},

		onLocalizationImportExportExportButtonClick: function () {
			var formValues = this.exportPanel.getForm().getValues();
			var params = {};
			params[CMDBuild.core.constants.Proxy.TYPE] = formValues[CMDBuild.core.constants.Proxy.TYPE];
			params[CMDBuild.core.constants.Proxy.SEPARATOR] = formValues[CMDBuild.core.constants.Proxy.SEPARATOR];
			params[CMDBuild.core.constants.Proxy.ACTIVE_ONLY] = formValues[CMDBuild.core.constants.Proxy.ACTIVE_ONLY];
			params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			CMDBuild.proxy.localization.Export.exports({
				form: this.exportPanel.getForm(),
				params: params
			});
		},

		/**
		 * ActiveOnly parameter is managed on server side only for class, domain, lookup and process
		 *
		 * @param {String} selection
		 */
		onLocalizationImportExportExportSectionChange: function (selection) {
			this.exportPanel.activeOnlyCheckbox.setValue();
			this.exportPanel.activeOnlyCheckbox.setDisabled(!Ext.Array.contains(this.activeOnlySections, selection));
		},

		onLocalizationImportExportExportShow: function () {

		},

		onLocalizationImportExportImportButtonClick: function () {
			if (this.validate(this.importPanel))
				CMDBuild.proxy.localization.Import.imports({
					form: this.importPanel.getForm(),
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						var importFailures = decodedResponse[CMDBuild.core.constants.Proxy.FAILURES];

						if (Ext.isEmpty(importFailures)) {
							CMDBuild.core.Message.success();
						} else if (Ext.isArray(importFailures)) {
							var data = [];

							Ext.Array.each(importFailures, function (failureObject, i, allFailureObjects) {
								if (Ext.isObject(failureObject) && !Ext.Object.isEmpty(failureObject))
									data.push([failureObject[CMDBuild.core.constants.Proxy.MESSAGE], failureObject[CMDBuild.core.constants.Proxy.RECORD]]);
							}, this);


							this.failuresWindow = Ext.create('CMDBuild.core.window.AbstractModal', {
								title: CMDBuild.Translation.common.failure,

								dockedItems: [
									Ext.create('Ext.toolbar.Toolbar', {
										dock: 'bottom',
										itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
										ui: 'footer',

										layout: {
											type: 'hbox',
											align: 'middle',
											pack: 'center'
										},

										items: [
											Ext.create('CMDBuild.core.buttons.text.Close', {
												scope: this,

												handler: function (button, e) {
													this.failuresWindow.destroy();
												}
											})
										]
									})
								],
								items: [
									Ext.create('Ext.grid.Panel', {
										border: false,
										frame: false,

										columns: [
											{ text: CMDBuild.Translation.message, dataIndex: CMDBuild.core.constants.Proxy.MESSAGE },
											{ text: CMDBuild.Translation.record, dataIndex: CMDBuild.core.constants.Proxy.RECORD, flex: 1 }
										],
										store: Ext.create('Ext.data.ArrayStore', {
											fields:[CMDBuild.core.constants.Proxy.MESSAGE, CMDBuild.core.constants.Proxy.RECORD],
											data: data
										})
									})
								]
							}).show();
						}
					},
					failure: function (response, options, decodedResponse) {
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
