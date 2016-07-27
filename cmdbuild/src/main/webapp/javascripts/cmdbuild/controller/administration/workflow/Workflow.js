(function () {

	Ext.define('CMDBuild.controller.administration.workflow.Workflow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowAddButtonClick',
			'onWorkflowModuleInit = onModuleInit',
			'onWorkflowPrintSchemaButtonClick = onButtonPrintClick',
			'workflowSelectedWorkflowGet',
			'workflowSelectedWorkflowIsEmpty',
			'workflowSelectedWorkflowReset',
			'workflowTabInit'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.controller.administration.classes.CMClassAttributeController}
		 */
		controllerAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.administration.workflow.tabs.Domains}
		 */
		controllerDomains: undefined,

		/**
		 * @property {CMDBuild.controller.administration.workflow.tabs.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.workflow.tabs.TaskManager}
		 */
		controllerTaks: undefined,

		/**
		 * @property {CMDBuild.model.workflow.Workflow}
		 *
		 * @private
		 */
		selectedWorkflow: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.workflow.WorkflowView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.workflow.WorkflowView', { delegate: this });

			// Shorthands
			this.tabPanel = this.view.tabPanel;

			this.tabPanel.removeAll();

			// Controller build
			this.controllerAttributes = Ext.create('CMDBuild.controller.administration.workflow.CMAttributes', { // TODO: legacy
				parentDelegate: this,
				view: this.view.attributesPanel
			});
			this.controllerDomains = Ext.create('CMDBuild.controller.administration.workflow.tabs.Domains', { parentDelegate: this });
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.workflow.tabs.Properties', { parentDelegate: this });
			this.controllerTasks = Ext.create('CMDBuild.controller.administration.workflow.tabs.TaskManager', { parentDelegate: this });

			// Inject tabs (sorted)
			this.tabPanel.add(this.controllerProperties.getView());
			this.tabPanel.add(this.view.attributesPanel); // TODO: legacy
			this.tabPanel.add(this.controllerDomains.getView());
			this.tabPanel.add(this.controllerTasks.getView());
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowAddButtonClick: function () {
			this.tabPanel.setActiveTab(0); // Must be first to avoid tab show errors

			this.cmfg('mainViewportAccordionDeselect', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());
			this.cmfg('workflowSelectedWorkflowReset');

			this.setViewTitle();

			this.controllerAttributes.onAddClassButtonClick(); // TODO: legacy
			this.controllerDomains.cmfg('onWorkflowTabDomainsAddWorkflowButtonClick');
			this.controllerProperties.cmfg('onWorkflowTabPropertiesAddWorkflowButtonClick');
			this.controllerTasks.cmfg('onWorkflowTabTasksAddWorkflowButtonClick');
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onWorkflowModuleInit: function (node) {
			if (!Ext.Object.isEmpty(node)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.workflow.Workflow.read({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

						var selectedWorkflow = Ext.Array.findBy(decodedResponse, function (workflowObject, i) {
							return node.get(CMDBuild.core.constants.Proxy.ENTITY_ID) == workflowObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedWorkflow)) {
							this.workflowSelectedWorkflowSet({ value: selectedWorkflow });

							this.setViewTitle(this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.DESCRIPTION));

							this.cmfg('workflowTabInit');

							this.tabPanel.setActiveTab(0);
							this.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
						} else {
							_error('workflow with id "' + node.get(CMDBuild.core.constants.Proxy.ENTITY_ID) + '" not found', this);
						}
					}
				});

				this.onModuleInit(node); // Custom callParent() implementation
			}
		},

		/**
		 * @param {String} format
		 *
		 * @returns {Void}
		 */
		onWorkflowPrintSchemaButtonClick: function (format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.FORMAT] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'schema',
					parameters: params
				});
			}
		},

		// SelectedWorkflow property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			workflowSelectedWorkflowGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWorkflow';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			workflowSelectedWorkflowIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWorkflow';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			workflowSelectedWorkflowReset: function (parameters) {
				this.propertyManageReset('selectedWorkflow');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			workflowSelectedWorkflowSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.workflow.Workflow';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWorkflow';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * TODO: use AbstractController forwarding methods on controllerAttributes controller refactor
		 */
		workflowTabInit: function () {
			this.controllerAttributes.onClassSelected( // TODO: legacy
				this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID),
				this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME));
			this.controllerDomains.cmfg('workflowTabInit');
			this.controllerProperties.cmfg('workflowTabInit');
			this.controllerTasks.cmfg('workflowTabInit');
		}
	});

})();
