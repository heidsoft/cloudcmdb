(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormWorkflowController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {String}
		 */
		taskType: 'workflow',

		/**
		 * @property {CMDBuild.view.administration.tasks.workflow.CMStep1}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @overwrite
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {String}
		 */
		checkWorkflowComboSelected: function() {
			return this.getWorkflowDelegate().getValueCombo();
		},

		// GETters functions
			/**
			 * @return {String}
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

			/**
			 * @return {CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController} delegate
			 */
			getWorkflowDelegate: function() {
				return this.view.workflowForm.delegate;
			},

			/**
			 * @return {Object}
			 */
			getValueWorkflowAttributeGrid: function() {
				return this.getWorkflowDelegate().getValueGrid();
			},

		/**
		 * To erase workflow form used on addButtonClick
		 */
		eraseWorkflowForm: function() {
			this.getWorkflowDelegate().eraseWorkflowForm();
		},

		// SETters functions
			/**
			 * @param {Boolean} state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledWorkflowAttributesGrid: function(state) {
				this.getWorkflowDelegate().setDisabledAttributesGrid(state);
			},

			/**
			 * @param {String} value
			 */
			setValueActive: function(value) {
				this.view.activeField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueWorkflowAttributesGrid: function(value) {
				this.getWorkflowDelegate().setValueGrid(value);
			},

			/**
			 * @param {String} value
			 */
			setValueWorkflowCombo: function(value) {
				this.getWorkflowDelegate().setValueCombo(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep1', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.workflow.CMStep1Delegate}
		 */
		delegate: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name: CMDBuild.core.constants.Proxy.TYPE,
				value: tr.tasksTypes.workflow,
				maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
				anchor: '100%',
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				submitValue: false
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.core.constants.Proxy.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.constants.Proxy.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
				anchor: '100%',
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.constants.Proxy.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
				anchor: '100%'
			});

			this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
				combo: {
					name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME
				},
				widthFixDisable: false
			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.workflowForm
				]
			});

			this.callParent(arguments);
		}
	});

})();