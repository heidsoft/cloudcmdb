(function () {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormGenericController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.proxy.taskManager.Generic'
		],

		/**
		 * @cfg {Array} array of all step delegates
		 */
		delegateStep: undefined,

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Number}
		 */
		selectedId: undefined,

		/**
		 * @cfg {Ext.selection.Model}
		 */
		selectionModel: undefined,

		/**
		 * @cfg {String}
		 */
		taskType: 'generic',

		/**
		 * @property {CMDBuild.view.administration.tasks.CMTasksForm}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @override
		 */
		cmOn: function (name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();

				case 'onRowSelected':
					return this.onRowSelected();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @override
		 */
		removeItem: function () {
			if (!Ext.isEmpty(this.selectedId)) {
				CMDBuild.core.LoadMask.show();

				CMDBuild.proxy.taskManager.Generic.remove({
					params: {
						id: this.selectedId
					},
					loadMask: false,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			}
		},

		/**
		 * @override
		 */
		onRowSelected: function () {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.core.constants.Proxy.ID);

				// Selected task asynchronous store query
				CMDBuild.proxy.taskManager.Generic.read({
					params: {
						id: this.selectedId
					},
					loadMask: false,
					scope: this,
					success: function (rensponse, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							var record = Ext.create('CMDBuild.model.taskManager.generic.Generic', decodedResponse);
							record.set(CMDBuild.core.constants.Proxy.CONTEXT, record.get(CMDBuild.core.constants.Proxy.CONTEXT)['client']); // FIXME: multiple sub-context predisposition

							this.parentDelegate.loadForm(this.taskType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find a working fix, so i must set all fields manually

							// Set step1 [0] data
							this.delegateStep[0].setValueActive(record.get(CMDBuild.core.constants.Proxy.ACTIVE));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.core.constants.Proxy.DESCRIPTION));
							this.delegateStep[0].setValueId(record.get(CMDBuild.core.constants.Proxy.ID));

							// Set step2 [1] data
							this.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.core.constants.Proxy.CRON_EXPRESSION));
							this.delegateStep[1].setValueBase(record.get(CMDBuild.core.constants.Proxy.CRON_EXPRESSION));

							// Set step3 [2] data
							this.delegateStep[2].setData(record.get(CMDBuild.core.constants.Proxy.CONTEXT));

							// Set step4 [3] data
//							this.delegateStep[3].setValueEmailFieldsetCheckbox(record.get(CMDBuild.core.constants.Proxy.EMAIL_ACTIVE));
							this.delegateStep[3].setValueEmailAccount(record.get(CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT));
							this.delegateStep[3].setValueEmailTemplate(record.get(CMDBuild.core.constants.Proxy.EMAIL_TEMPLATE));

							// Set step5 [4] data
							this.delegateStep[4].setValueReportAttributesGrid(record.get(CMDBuild.core.constants.Proxy.REPORT_PARAMETERS));
							this.delegateStep[4].setValueReportCombo(record.get(CMDBuild.core.constants.Proxy.REPORT_NAME));
							this.delegateStep[4].setValueReportExtension(record.get(CMDBuild.core.constants.Proxy.REPORT_EXTENSION));
							this.delegateStep[4].setValueReportFieldsetCheckbox(record.get(CMDBuild.core.constants.Proxy.REPORT_ACTIVE));

							this.view.disableModify(true);
						}
					}
				});

				this.parentDelegate.changeItem(0);
			}
		},

		/**
		 * @override
		 */
		onSaveButtonClick: function () {
			var formData = this.view.getData(true);
			var submitDatas = {};

			// Validate before save
			if (this.validate(formData[CMDBuild.core.constants.Proxy.ACTIVE])) {
				CMDBuild.core.LoadMask.show();

				// Data filtering to submit only right values
				submitDatas[CMDBuild.core.constants.Proxy.ACTIVE] = formData[CMDBuild.core.constants.Proxy.ACTIVE];
				submitDatas[CMDBuild.core.constants.Proxy.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue();
				submitDatas[CMDBuild.core.constants.Proxy.DESCRIPTION] = formData[CMDBuild.core.constants.Proxy.DESCRIPTION];
				submitDatas[CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT] = formData[CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT];
				submitDatas[CMDBuild.core.constants.Proxy.EMAIL_ACTIVE] = true; // Fixed value untill refactor
				submitDatas[CMDBuild.core.constants.Proxy.EMAIL_TEMPLATE] = formData[CMDBuild.core.constants.Proxy.EMAIL_TEMPLATE];
				submitDatas[CMDBuild.core.constants.Proxy.ID] = formData[CMDBuild.core.constants.Proxy.ID];

				var contextData = this.delegateStep[2].getData();
				if (!Ext.isEmpty(contextData))
					submitDatas[CMDBuild.core.constants.Proxy.CONTEXT] = Ext.encode({ client: contextData }); // FIXME: multiple sub-context predisposition

				// Fieldset submitting filter to avoid to send datas if fieldset are collapsed
					var reportFieldsetCheckboxValue = this.delegateStep[4].getValueReportFieldsetCheckbox();
					if (reportFieldsetCheckboxValue) {
						var attributesGridValues = this.delegateStep[4].getValueReportAttributeGrid();

						if (!Ext.Object.isEmpty(attributesGridValues))
							submitDatas[CMDBuild.core.constants.Proxy.REPORT_PARAMETERS] = Ext.encode(attributesGridValues);

						submitDatas[CMDBuild.core.constants.Proxy.REPORT_ACTIVE] = reportFieldsetCheckboxValue;
						submitDatas[CMDBuild.core.constants.Proxy.REPORT_EXTENSION] = formData[CMDBuild.core.constants.Proxy.REPORT_EXTENSION];
						submitDatas[CMDBuild.core.constants.Proxy.REPORT_NAME] = formData[CMDBuild.core.constants.Proxy.REPORT_NAME];
					}

				if (Ext.isEmpty(formData[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.proxy.taskManager.Generic.create({
						params: submitDatas,
						loadMask: false,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.proxy.taskManager.Generic.update({
						params: submitDatas,
						loadMask: false,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				}
			}
		},

		/**
		 * Task validation
		 *
		 * @param {Boolean} enable
		 *
		 * @return {Boolean}
		 *
		 * @override
		 */
		validate: function (enable) {
			// Cron field validation
			this.delegateStep[1].getCronDelegate().validate(enable);

			// Report fieldset validation
			this.delegateStep[4].getReportDelegate().cmfg('onTaskManagerReportFormValidationEnable', (
				enable && this.delegateStep[4].getValueReportFieldsetCheckbox()
			));

			return this.callParent(arguments);
		}
	});

})();
