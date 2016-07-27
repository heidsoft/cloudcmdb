(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
		extend: 'Ext.form.FieldContainer',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController}
		 */
		delegate: undefined,

		border: false,
		considerAsFieldToDisable: true,
		fieldLabel: tr.workflow,
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param {Object} configuration
		 * @param {Object} configuration.combo
		 * @param {Object} configuration.grid
		 * @param {Object} configuration.widthFixDisable
		 */
		constructor: function(configuration) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', this);

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.combo)) {
				this.comboConfig = { delegate: this.delegate };
			} else {
				this.comboConfig = configuration.combo;
				this.comboConfig.delegate = this.delegate;
			}

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.grid)) {
				this.gridConfig = { delegate: this.delegate };
			} else {
				this.gridConfig = configuration.grid;
				this.gridConfig.delegate = this.delegate;
			}

			if (Ext.isEmpty(configuration.widthFixDisable))
				this.delegate.fieldWidthsFix();

			this.callParent(arguments);
		},

		initComponent: function() {
			this.combo = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', this.comboConfig);
			this.delegate.comboField = this.combo;

			this.grid = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid', this.gridConfig);
			this.delegate.gridField = this.grid;
			this.delegate.gridEditorPlugin = this.grid.gridEditorPlugin;

			Ext.apply(this, {
				items: [this.combo, this.grid]
			});

			this.callParent(arguments);
		}
	});

})();