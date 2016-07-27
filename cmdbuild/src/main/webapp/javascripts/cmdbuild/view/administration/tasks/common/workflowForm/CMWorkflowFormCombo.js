(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
		extend: 'Ext.form.field.ComboBox',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.taskManager.common.WorkflowForm'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 *
		 * @required
		 */
		name: undefined,

		valueField: CMDBuild.core.constants.Proxy.NAME,
		displayField: CMDBuild.core.constants.Proxy.TEXT,
		maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
		forceSelection: true,
		editable: false,

		store: CMDBuild.proxy.taskManager.common.WorkflowForm.getStore(),
		queryMode: 'local',

		listeners: {
			select: function(combo, records, eOpts) {
				this.delegate.cmOn('onSelectWorkflow', true);
			}
		}
	});

})();