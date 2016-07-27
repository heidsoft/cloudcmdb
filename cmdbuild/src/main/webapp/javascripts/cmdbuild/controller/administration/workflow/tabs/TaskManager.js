(function () {

	Ext.define('CMDBuild.controller.administration.workflow.tabs.TaskManager', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.workflow.Tasks'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.Workflow}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowTabTasksAddButtonClick',
			'onWorkflowTabTasksAddWorkflowButtonClick',
			'onWorkflowTabTasksItemDoubleClick',
			'onWorkflowTabTasksModifyButtonClick',
			'onWorkflowTabTasksRemoveButtonClick',
			'onWorkflowTabTasksRowSelect',
			'onWorkflowTabTasksShow',
			'workflowTabTasksInit = workflowTabInit'
		],

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.taskManager.GridPanel}
		 */
		grid: undefined,

		/**
		 * Just the grid subset of task properties, not a full task object
		 *
		 * @property {CMDBuild.model.workflow.tabs.taskManager.Grid}
		 *
		 * @private
		 */
		selectedTask: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.workflow.tabs.taskManager.TaskManagerView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.workflow.Workflow} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.workflow.tabs.taskManager.TaskManagerView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabTasksAddButtonClick: function () {
			this.cmfg('mainViewportAccordionDeselect', 'task');
			this.cmfg('mainViewportAccordionControllerGet', 'task').disableStoreLoad = true;
			this.cmfg('mainViewportAccordionControllerExpand', 'task');

			this.cmfg('mainViewportAccordionControllerGet', 'task').getView().on('storeload', function (accordion, eOpts) {
				Ext.Function.createDelayed(function () {
					this.cmfg('mainViewportModuleControllerGet', 'task').cmOn('onAddButtonClick', { type: 'workflow' });
				}, 100, this)();
			}, this, { single: true });

			this.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: 'task',
				nodeIdToSelect: 'accordion-task-workflow'
			});
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabTasksAddWorkflowButtonClick: function () {
			this.view.disable();
		},

		/**
		 * On this kind of grids item double click only selects row in relative target grid
		 *
		 * @returns {Void}
		 */
		onWorkflowTabTasksItemDoubleClick: function () {
			if (!this.selectedTaskIsEmpty()) {
				this.cmfg('mainViewportAccordionDeselect', 'task');
				this.cmfg('mainViewportAccordionControllerGet', 'task').disableStoreLoad = true;
				this.cmfg('mainViewportAccordionControllerExpand', 'task');

				// Add action to act as expand callback
				this.cmfg('mainViewportAccordionControllerGet', 'task').getView().on('selectionchange', function (accordion, eOpts) {
					Ext.Function.createDelayed(function () { // Delay needed because of server asynchronous call to get domain data
						if (!Ext.isEmpty(this.cmfg('mainViewportModuleControllerGet', 'task').form.delegate))
							this.cmfg('mainViewportModuleControllerGet', 'task').grid.getStore().load({
								scope: this,
								callback: function (records, operation, success) {
									var selectionIndex = this.cmfg('mainViewportModuleControllerGet', 'task').grid.getStore().find(
										CMDBuild.core.constants.Proxy.ID,
										this.selectedTaskGet(CMDBuild.core.constants.Proxy.ID)
									);

									if (selectionIndex >= 0) {
										this.cmfg('mainViewportModuleControllerGet', 'task').grid.getSelectionModel().select(selectionIndex, true);
									} else {
										_error('cannot find taks with id ' + this.selectedTaskGet(CMDBuild.core.constants.Proxy.ID) + ' in store', this);

										this.cmfg('mainViewportModuleControllerGet', 'task').form.delegate.selectedId = null;
										this.cmfg('mainViewportModuleControllerGet', 'task').form.disableModify();
									}
								}
							});
					}, 100, this)();
				}, this, { single: true });

				this.cmfg('mainViewportAccordionControllerUpdateStore', {
					identifier: 'task',
					nodeIdToSelect: 'accordion-task-workflow'
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabTasksRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabTasksModifyButtonClick: function () {
			if (!this.selectedTaskIsEmpty()) {
				this.cmfg('mainViewportAccordionDeselect', 'task');
				this.cmfg('mainViewportAccordionControllerGet', 'task').disableStoreLoad = true;
				this.cmfg('mainViewportAccordionControllerExpand', 'task');

				// Add action to act as expand callback
				this.cmfg('mainViewportAccordionControllerGet', 'task').getView().on('selectionchange', function (accordion, eOpts) {
					Ext.Function.createDelayed(function () { // Delay needed because of server asynchronous call to get domain data
						if (!Ext.isEmpty(this.cmfg('mainViewportModuleControllerGet', 'task').form.delegate))
							this.cmfg('mainViewportModuleControllerGet', 'task').grid.getStore().load({
								scope: this,
								callback: function (records, operation, success) {
									var selectionIndex = this.cmfg('mainViewportModuleControllerGet', 'task').grid.getStore().find(
										CMDBuild.core.constants.Proxy.ID,
										this.selectedTaskGet(CMDBuild.core.constants.Proxy.ID)
									);

									if (selectionIndex >= 0) {
										this.cmfg('mainViewportModuleControllerGet', 'task').grid.getSelectionModel().select(selectionIndex, true);

										Ext.Function.createDelayed(function () {
											this.cmfg('mainViewportModuleControllerGet', 'task').form.delegate.cmOn('onModifyButtonClick');
										}, 100, this)();
									} else {
										_error('cannot find taks with id ' + this.selectedTaskGet(CMDBuild.core.constants.Proxy.ID) + ' in store', this);

										this.cmfg('mainViewportModuleControllerGet', 'task').form.delegate.selectedId = null;
										this.cmfg('mainViewportModuleControllerGet', 'task').form.disableModify();
									}
								}
							});
					}, 100, this)();
				}, this, { single: true });

				this.cmfg('mainViewportAccordionControllerUpdateStore', {
					identifier: 'task',
					nodeIdToSelect: 'accordion-task-workflow'
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabTasksRowSelect: function () {
			if (this.grid.getSelectionModel().hasSelection())
				this.selectedTaskSet({ value: this.grid.getSelectionModel().getSelection()[0] });

			this.view.setDisabledTopBar(!this.grid.getSelectionModel().hasSelection());
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabTasksShow: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

			this.grid.getStore().load({
				params: params,
				scope: this,
				callback: function (records, operation, success) {
					if (!this.grid.getSelectionModel().hasSelection())
						this.grid.getSelectionModel().select(0, true);

					this.cmfg('onWorkflowTabTasksRowSelect');
				}
			});

		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		removeItem: function () {
			if (!this.selectedTaskIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.selectedTaskGet(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.workflow.Tasks.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.cmfg('onWorkflowTabTasksShow');
					}
				});
			}
		},

		// SelectedTask property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			selectedTaskGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTask';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			selectedTaskIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTask';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			selectedTaskReset: function (parameters) {
				this.propertyManageReset('selectedTask');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			selectedTaskSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.workflow.tabs.taskManager.Grid';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTask';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * Enable/Disable tab on workflow selection
		 *
		 * @returns {Void}
		 */
		workflowTabTasksInit: function () {
			this.view.setDisabled(
				this.cmfg('workflowSelectedWorkflowIsEmpty')
				|| this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.IS_SUPER_CLASS)
				|| this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.TABLE_TYPE) == CMDBuild.core.constants.Global.getTableTypeSimpleTable()
			);
		}
	});

})();
