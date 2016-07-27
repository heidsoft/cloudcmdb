(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormEmailController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.email.CMStep4}
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
			 * @return {Boolean}
			 */
			getValueWorkflowFieldsetCheckbox: function() {
				return this.view.workflowFieldset.checkboxCmp.getValue();
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
			setDisabledWorkflowAttributesGrid: function(state) {
				this.getWorkflowDelegate().setDisabledAttributesGrid(state);
			},

			/**
			 * @param {Object} value
			 */
			setValueWorkflowAttributesGrid: function(value) {
				this.getWorkflowDelegate().setValueGrid(value);
			},

			/**
			 * @param {String} value
			 */
			setValueWorkflowCombo: function(value) {
				this.getWorkflowDelegate().setValueCombo(value);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueWorkflowFieldsetCheckbox: function(state) {
				if (state) {
					this.view.workflowFieldset.expand();
				} else {
					this.view.workflowFieldset.collapse();
				}
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.email.CMStep4Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		workflowFieldset: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm}
		 */
		workflowForm: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep4Delegate', this);

			this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
				combo: {
					name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME
				}
			});

			this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.startWorkflow,
				checkboxName: CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE,
				checkboxToggle: true,
				collapsed: true,
				collapsible: true,
				toggleOnTitleClick: true,
				overflowY: 'auto',

				items: [this.workflowForm]
			});

			this.workflowFieldset.fieldWidthsFix();

			Ext.apply(this, {
				items: [this.workflowFieldset]
			});

			this.callParent(arguments);
		}
	});

})();