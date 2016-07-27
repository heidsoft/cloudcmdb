(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormConnectorController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.connector.CMStep1}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		taskType: 'connector',

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param (Function) callback
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

		// GETters functions
			/**
			 * @return {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController} delegate
			 */
			getNotificationDelegate: function() {
				return this.view.notificationForm.delegate;
			},

			/**
			 * @return {String}
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

			/**
			 * @return {Boolean}
			 */
			getValueNotificationFieldsetCheckbox: function() {
				return this.view.notificationFieldset.checkboxCmp.getValue();
			},

		// GETters functions
			/**
			 * @param {Boolean} state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param {Boolean} value
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
			 * @param {Int} value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			},

			/**
			 * @param {Object} value
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
			setValueNotificationTemplateError: function(value) {
				this.getNotificationDelegate().setValue('templateError', value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep1', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.connector.CMStep1Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		descriptionField: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		idField: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		notificationFieldset: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm}
		 */
		notificationForm: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		typeField: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
			anchor: '100%'
		},

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name: CMDBuild.core.constants.Proxy.TYPE,
				value: tr.tasksTypes.connector,
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
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.constants.Proxy.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
			});

			// Email notification configuration
				this.notificationForm = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
					sender: {
						type: 'sender',
						disabled: false
					},
					templateError: {
						type: 'template',
						disabled: false,
						fieldLabel: tr.notificationForm.templateError,
						name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE_ERROR
					}
				});

				this.notificationFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.notificationForm.titlePlur,
					checkboxName: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,
					maxWidth: '100%',

					items: [this.notificationForm]
				});

				this.notificationFieldset.fieldWidthsFix();
			// END: Email notification configuration

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.notificationFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();