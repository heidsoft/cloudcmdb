(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormEmailController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.proxy.taskManager.Email',
			'CMDBuild.model.CMModelTasks'
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
		 * @property {Int}
		 */
		selectedId: undefined,

		/**
		 * @property {Ext.selection.Model}
		 */
		selectionModel: undefined,

		/**
		 * @cfg {String}
		 */
		taskType: 'email',

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
		cmOn: function(name, param, callBack) {
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
		removeItem: function() {
			if (!Ext.isEmpty(this.selectedId)) {
				CMDBuild.core.LoadMask.show();

				CMDBuild.proxy.taskManager.Email.remove({
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
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @override
		 */
		onAddButtonClick: function(name, param, callBack) {
			this.callParent(arguments);

			this.delegateStep[3].eraseWorkflowForm();
		},

		/**
		 * @override
		 */
		onModifyButtonClick: function() {
			this.callParent(arguments);

			if (!this.delegateStep[3].checkWorkflowComboSelected())
				this.delegateStep[3].setDisabledWorkflowAttributesGrid(true);
		},

		/**
		 * @override
		 */
		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.core.constants.Proxy.ID);

				// Selected task asynchronous store query
				CMDBuild.proxy.taskManager.Email.read({
					params: {
						id: this.selectedId
					},
					loadMask: false,
					scope: this,
					success: function(rensponse, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							var record = Ext.create('CMDBuild.model.CMModelTasks.singleTask.email', decodedResponse);

							this.parentDelegate.loadForm(this.taskType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find a working fix, so i must set all fields manually

							// Set step1 [0] datas
							this.delegateStep[0].setValueActive(record.get(CMDBuild.core.constants.Proxy.ACTIVE));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.core.constants.Proxy.DESCRIPTION));
							this.delegateStep[0].setValueEmailAccount(record.get(CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT));
							this.delegateStep[0].setValueFilterType(record.get(CMDBuild.core.constants.Proxy.FILTER_TYPE));
							this.delegateStep[0].setValueFilterFunction(record.get(CMDBuild.core.constants.Proxy.FILTER_FUNCTION));
							this.delegateStep[0].setValueFilterFromAddress(
								this.delegateStep[0].getFromAddressFilterDelegate().filterStringBuild(
									record.get(CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS)
								)
							);
							this.delegateStep[0].setValueFilterSubject(
								this.delegateStep[0].getSubjectFilterDelegate().filterStringBuild(
									record.get(CMDBuild.core.constants.Proxy.FILTER_SUBJECT)
								)
							);
							this.delegateStep[0].setValueId(record.get(CMDBuild.core.constants.Proxy.ID));
							this.delegateStep[0].setValueIncomingFolder(record.get(CMDBuild.core.constants.Proxy.INCOMING_FOLDER));
							this.delegateStep[0].setValueProcessedFolder(record.get(CMDBuild.core.constants.Proxy.PROCESSED_FOLDER));
							this.delegateStep[0].setValueRejectedFieldsetCheckbox(record.get(CMDBuild.core.constants.Proxy.REJECT_NOT_MATCHING));
							this.delegateStep[0].setValueRejectedFolder(record.get(CMDBuild.core.constants.Proxy.REJECTED_FOLDER));

							// Set step2 [1] datas
							this.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.core.constants.Proxy.CRON_EXPRESSION));
							this.delegateStep[1].setValueBase(record.get(CMDBuild.core.constants.Proxy.CRON_EXPRESSION));

							// Set step3 [2] datas
							this.delegateStep[2].setValueAttachmentsFieldsetCheckbox(record.get(CMDBuild.core.constants.Proxy.ATTACHMENTS_ACTIVE));
							this.delegateStep[2].setValueAttachmentsCombo(record.get(CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY));
							this.delegateStep[2].setValueNotificationFieldsetCheckbox(record.get(CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE));
							this.delegateStep[2].setValueNotificationTemplate(record.get(CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE));
							this.delegateStep[2].setValueParsingFieldsetCheckbox(record.get(CMDBuild.core.constants.Proxy.PARSING_ACTIVE));
							this.delegateStep[2].setValueParsingFields(
								record.get(CMDBuild.core.constants.Proxy.PARSING_KEY_INIT),
								record.get(CMDBuild.core.constants.Proxy.PARSING_KEY_END),
								record.get(CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT),
								record.get(CMDBuild.core.constants.Proxy.PARSING_VALUE_END)
							);

							// Set step4 [3] datas
							this.delegateStep[3].setValueWorkflowAttributesGrid(record.get(CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES));
							this.delegateStep[3].setValueWorkflowCombo(record.get(CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME));
							this.delegateStep[3].setValueWorkflowFieldsetCheckbox(record.get(CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE));

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
		onSaveButtonClick: function() {
			var formData = this.view.getData(true);
			var submitDatas = {};

			// Validate before save
			if (this.validate(formData[CMDBuild.core.constants.Proxy.ACTIVE])) {
				CMDBuild.core.LoadMask.show();

				submitDatas[CMDBuild.core.constants.Proxy.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue();

				// Fieldset submitting filter to avoid to send datas if fieldset are collapsed
					var rejectedFieldsetCheckboxValue = this.delegateStep[0].getValueRejectedFieldsetCheckbox();
					if (rejectedFieldsetCheckboxValue) {
						submitDatas[CMDBuild.core.constants.Proxy.REJECT_NOT_MATCHING] = rejectedFieldsetCheckboxValue;
						submitDatas[CMDBuild.core.constants.Proxy.REJECTED_FOLDER] = formData[CMDBuild.core.constants.Proxy.REJECTED_FOLDER];
					}

					var filterFieldsetComboValue = this.delegateStep[0].view.filterTypeCombobox.getValue();
					if (filterFieldsetComboValue) {
						submitDatas[CMDBuild.core.constants.Proxy.FILTER_TYPE] = formData[CMDBuild.core.constants.Proxy.FILTER_TYPE];

						switch (filterFieldsetComboValue) {
							case 'function': {
								submitDatas[CMDBuild.core.constants.Proxy.FILTER_FUNCTION] = formData[CMDBuild.core.constants.Proxy.FILTER_FUNCTION];
							} break;

							case 'regex': {
								// Form submit values formatting
								if (!Ext.isEmpty(formData.filterFromAddress))
									submitDatas[CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS] = Ext.encode(
										formData[CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS].split(
											this.delegateStep[0].getFromAddressFilterDelegate().getTextareaConcatParameter()
										)
									);

								if (!Ext.isEmpty(formData.filterSubject))
									submitDatas[CMDBuild.core.constants.Proxy.FILTER_SUBJECT] = Ext.encode(
										formData[CMDBuild.core.constants.Proxy.FILTER_SUBJECT].split(
											this.delegateStep[0].getSubjectFilterDelegate().getTextareaConcatParameter()
										)
									);
							} break;
						}
					}

					var attachmentsFieldsetCheckboxValue = this.delegateStep[2].getValueAttachmentsFieldsetCheckbox();
					if (attachmentsFieldsetCheckboxValue) {
						submitDatas[CMDBuild.core.constants.Proxy.ATTACHMENTS_ACTIVE] = attachmentsFieldsetCheckboxValue;
						submitDatas[CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY] = formData[CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY];
					}

					var notificationFieldsetCheckboxValue = this.delegateStep[2].getValueNotificationFieldsetCheckbox();
					if (notificationFieldsetCheckboxValue) {
						submitDatas[CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE] = notificationFieldsetCheckboxValue;
						submitDatas[CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE] = formData[CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE];
					}

					var parsingFieldsetCheckboxValue = this.delegateStep[2].getValueParsingFieldsetCheckbox();
					if (parsingFieldsetCheckboxValue) {
						submitDatas[CMDBuild.core.constants.Proxy.PARSING_ACTIVE] = parsingFieldsetCheckboxValue;
						submitDatas[CMDBuild.core.constants.Proxy.PARSING_KEY_END] = formData[CMDBuild.core.constants.Proxy.PARSING_KEY_END];
						submitDatas[CMDBuild.core.constants.Proxy.PARSING_KEY_INIT] = formData[CMDBuild.core.constants.Proxy.PARSING_KEY_INIT];
						submitDatas[CMDBuild.core.constants.Proxy.PARSING_VALUE_END] = formData[CMDBuild.core.constants.Proxy.PARSING_VALUE_END];
						submitDatas[CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT] = formData[CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT];
					}

					var workflowFieldsetCheckboxValue = this.delegateStep[3].getValueWorkflowFieldsetCheckbox();
					if (workflowFieldsetCheckboxValue) {
						var attributesGridValues = this.delegateStep[3].getValueWorkflowAttributeGrid();

						if (!Ext.Object.isEmpty(attributesGridValues))
							submitDatas[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

						submitDatas[CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE] = workflowFieldsetCheckboxValue;
						submitDatas[CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME] = formData[CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME];
					}

				// Data filtering to submit only right values
				submitDatas[CMDBuild.core.constants.Proxy.ACTIVE] = formData[CMDBuild.core.constants.Proxy.ACTIVE];
				submitDatas[CMDBuild.core.constants.Proxy.DESCRIPTION] = formData[CMDBuild.core.constants.Proxy.DESCRIPTION];
				submitDatas[CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT] = formData[CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT];
				submitDatas[CMDBuild.core.constants.Proxy.ID] = formData[CMDBuild.core.constants.Proxy.ID];
				submitDatas[CMDBuild.core.constants.Proxy.INCOMING_FOLDER] = formData[CMDBuild.core.constants.Proxy.INCOMING_FOLDER];
				submitDatas[CMDBuild.core.constants.Proxy.PROCESSED_FOLDER] = formData[CMDBuild.core.constants.Proxy.PROCESSED_FOLDER];

				if (Ext.isEmpty(formData[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.proxy.taskManager.Email.create({
						params: submitDatas,
						loadMask: false,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.proxy.taskManager.Email.update({
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
		validate: function(enable) {
			// Email account and forlders validation
			this.delegateStep[0].setAllowBlankEmailAccountCombo(!enable);
			this.delegateStep[0].setAllowBlankIncomingFolder(!enable);
			this.delegateStep[0].setAllowBlankProcessedFolder(!enable);

			// Rejected folder validation
			this.delegateStep[0].setAllowBlankRejectedFolder(!this.delegateStep[0].getValueRejectedFieldsetCheckbox());

			// Cron field validation
			this.delegateStep[1].getCronDelegate().validate(enable);

			// Parsing validation
			if (this.delegateStep[2].getValueParsingFieldsetCheckbox() && enable) {
				this.delegateStep[2].setAllowBlankParsingFields(false);
			} else {
				this.delegateStep[2].setAllowBlankParsingFields(true);
			}

			// Notification validation
			this.delegateStep[2].getNotificationDelegate().validate(
				this.delegateStep[2].getValueNotificationFieldsetCheckbox()
				&& enable
			);

			// Attachments validation
			if (this.delegateStep[2].getValueAttachmentsFieldsetCheckbox() && enable) {
				this.delegateStep[2].setAllowBlankAttachmentsField(false);
			} else {
				this.delegateStep[2].setAllowBlankAttachmentsField(true);
			}

			// Workflow form validation
			this.delegateStep[3].getWorkflowDelegate().validate(
				this.delegateStep[3].getValueWorkflowFieldsetCheckbox()
				&& enable
			);

			return this.callParent(arguments);
		}
	});

})();
