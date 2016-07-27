(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep4Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormEventController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.event.asynchronous.CMStep4}
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
			return this.getValueWorkflowCombo();
		},

		// GETters functions
			/**
			 * @return {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController} delegate
			 */
			getNotificationDelegate: function() {
				return this.view.notificationForm.delegate;
			},

			/**
			 * @return {CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController} delegate
			 */
			getWorkflowDelegate: function() {
				return this.view.workflowForm.delegate;
			},

			/**
			 * @return {Boolean}
			 */
			getValueNotificationFieldsetCheckbox: function() {
				return this.view.notificationFieldset.checkboxCmp.getValue();
			},

			/**
			 * @return {Object}
			 */
			getValueWorkflowAttributeGrid: function() {
				return this.getWorkflowDelegate().getValueGrid();
			},

			/**
			 * @return {String}
			 */
			getValueWorkflowCombo: function() {
				return this.getWorkflowDelegate().getValueCombo();
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
			 * @param {String} value
			 */
			setValueNotificationAccount: function(value) {
				this.getNotificationDelegate().setValue('sender', value);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueNotificationFieldsetCheckbox: function(state) {
				if (state) {
					this.view.notificationFieldset.expand();
				} else {
					this.view.notificationFieldset.collapse();
				}
			},

			/**
			 * @param {String} value
			 */
			setValueNotificationTemplate: function(value) {
				this.getNotificationDelegate().setValue('template', value);
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

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep4', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.event.asynchronous.CMStep4Delegate}
		 */
		delegate: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep4Delegate', this);

			// Email notification configuration
				this.notificationForm = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
					sender: {
						type: 'sender',
						disabled: false
					},
					template: {
						type: 'template',
						disabled: false
					}
				});

				this.notificationFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.notificationForm.title,
					checkboxName: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,
					overflowY: 'auto',

					items: [this.notificationForm]
				});
			// END: Email notification configuration

			// Workflow configuration
				this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
					combo: {
						name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME
					}
				});

				this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.startWorkflow,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,
					overflowY: 'auto',

					items: [this.workflowForm]
				});
			// END: Workflow configuration

			Ext.apply(this, {
				items: [
					this.notificationFieldset
// TODO: future implementation
//					,
//					this.workflowFieldset
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable attribute table to correct malfunction that enables on class select
			 */
			activate: function(view, eOpts) {
				if (!this.delegate.checkWorkflowComboSelected())
					this.delegate.setDisabledWorkflowAttributesGrid(true);
			}
		}
	});

})();