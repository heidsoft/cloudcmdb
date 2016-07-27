(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormWorkflowController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.proxy.taskManager.Workflow',
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
		taskType: 'workflow',

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

				CMDBuild.proxy.taskManager.Workflow.remove({
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

			this.delegateStep[0].eraseWorkflowForm();
		},

		/**
		 * @override
		 */
		onModifyButtonClick: function() {
			this.callParent(arguments);

			if (!this.delegateStep[0].checkWorkflowComboSelected())
				this.delegateStep[0].setDisabledWorkflowAttributesGrid(true);
		},

		/**
		 * @override
		 */
		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.core.constants.Proxy.ID);

				// Selected task asynchronous store query
				CMDBuild.proxy.taskManager.Workflow.read({
					params: {
						id: this.selectedId
					},
					loadMask: false,
					scope: this,
					success: function(rensponse, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							var record = Ext.create('CMDBuild.model.CMModelTasks.singleTask.workflow', decodedResponse);

							this.parentDelegate.loadForm(this.taskType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find good fix, so i must set all fields manually

							// Set step1 [0] datas
							this.delegateStep[0].setValueActive(record.get(CMDBuild.core.constants.Proxy.ACTIVE));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.core.constants.Proxy.DESCRIPTION));
							this.delegateStep[0].setValueId(record.get(CMDBuild.core.constants.Proxy.ID));
							this.delegateStep[0].setValueWorkflowAttributesGrid(record.get(CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES));
							this.delegateStep[0].setValueWorkflowCombo(record.get(CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME));

							// Set step2 [1] datas
							this.delegateStep[1].setValueBase(record.get(CMDBuild.core.constants.Proxy.CRON_EXPRESSION));
							this.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.core.constants.Proxy.CRON_EXPRESSION));

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
			var attributesGridValues = this.delegateStep[0].getValueWorkflowAttributeGrid();
			var formData = this.view.getData(true);
			var submitDatas = {};

			// Validate before save
			if (this.validate(formData[CMDBuild.core.constants.Proxy.ACTIVE])) {
				CMDBuild.core.LoadMask.show();

				submitDatas[CMDBuild.core.constants.Proxy.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue();

				// Form submit values formatting
				if (!Ext.Object.isEmpty(attributesGridValues))
					submitDatas[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

				// Data filtering to submit only right values
				submitDatas[CMDBuild.core.constants.Proxy.ACTIVE] = formData[CMDBuild.core.constants.Proxy.ACTIVE];
				submitDatas[CMDBuild.core.constants.Proxy.DESCRIPTION] = formData[CMDBuild.core.constants.Proxy.DESCRIPTION];
				submitDatas[CMDBuild.core.constants.Proxy.ID] = formData[CMDBuild.core.constants.Proxy.ID];
				submitDatas[CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME] = formData[CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME];

				if (Ext.isEmpty(formData[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.proxy.taskManager.Workflow.create({
						params: submitDatas,
						loadMask: false,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.proxy.taskManager.Workflow.update({
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
			// Workflow form validation
			this.delegateStep[0].getWorkflowDelegate().validate(enable);

			// Cron field validation
			this.delegateStep[1].getCronDelegate().validate(enable);

			return this.callParent(arguments);
		}
	});

})();