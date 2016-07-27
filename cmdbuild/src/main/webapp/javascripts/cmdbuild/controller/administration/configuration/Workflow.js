(function () {

	Ext.define('CMDBuild.controller.administration.configuration.Workflow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.configuration.Workflow',
			'CMDBuild.model.configuration.Workflow'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationWorkflowSaveButtonClick',
			'onConfigurationWorkflowTabShow = onConfigurationWorkflowAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.WorkflowPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.WorkflowPanel', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationWorkflowSaveButtonClick: function () {
			CMDBuild.proxy.configuration.Workflow.update({
				params: CMDBuild.model.configuration.Workflow.convertToLegacy(this.view.getData(true)),
				scope: this,
				success: function (response, options, decodedResponse) {
					this.cmfg('onConfigurationWorkflowTabShow');

					CMDBuild.core.Message.success();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationWorkflowTabShow: function () {
			CMDBuild.proxy.configuration.Workflow.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse)) {
						this.view.loadRecord(Ext.create('CMDBuild.model.configuration.Workflow', CMDBuild.model.configuration.Workflow.convertFromLegacy(decodedResponse)));

						Ext.create('CMDBuild.core.configurations.builder.Workflow', { // Rebuild configuration model
							scope: this,
							callback: function (options, success, response) {
								this.cmfg('mainViewportAccordionSetDisabled', {
									identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow(),
									state: !CMDBuild.configuration.workflow.get(CMDBuild.core.constants.Proxy.ENABLED)
								});
							}
						});
					}
				}
			});
		}
	});

})();
