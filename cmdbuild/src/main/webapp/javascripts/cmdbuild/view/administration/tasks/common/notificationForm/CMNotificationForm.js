(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
		extend: 'Ext.container.Container',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController}
		 */
		delegate: undefined,

		/**
		 * @property {Array}
		 */
		inputFields: [],

		/**
		 * @proeprty {Object}
		 */
		inputFieldsConfiguration: undefined,

		/**
		 * Used to validate notification field types
		 *
		 * @cfg {Array}
		 */
		notificationFieldType: ['sender', 'template'],

		border: false,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		/**
		 * To check and acquire input fields configurations
		 *
		 * @param {Object} configuration
		 * 		ex: {
		 * 			notificationInputIdentifier1: {
		 * 				type: 'sender',
		 * 				delegate: (Object),
		 * 				fieldLabel: (String),
		 * 				...
		 * 			},
		 * 			notificationInputIdentifier2: {
		 * 				type: 'template',
		 * 				delegate: (Object),
		 * 				fieldLabel: (String),
		 * 				...
		 * 			},
		 * 		}
		 */
		constructor: function(configuration) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController', this);

			if (!Ext.isEmpty(configuration)) {
				for (var itemConfig in configuration) {
					if (
						Ext.isEmpty(configuration[itemConfig])
						|| this.notificationFieldType.indexOf(configuration[itemConfig].type) < 0
					) {
						delete configuration[itemConfig];
					} else {
						// Default configuration: delegate
						if (Ext.isEmpty(configuration[itemConfig].delegate))
							configuration[itemConfig].delegate = this.delegate;

						// Default configuration: disabled
						if (Ext.isEmpty(configuration[itemConfig].disabled))
							configuration[itemConfig].disabled = true;
					}
				}

				if (!Ext.isEmpty(configuration))
					this.inputFieldsConfiguration = configuration;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			this.inputFields = []; // Buffer reset
			this.delegate.inputFields = []; // Delegate inputFields buffer reset

			if (!Ext.isEmpty(this.inputFieldsConfiguration)) {
				for (var itemIndex in this.inputFieldsConfiguration) {
					var itemConfig = this.inputFieldsConfiguration[itemIndex];
					var inputField = null;

					switch (itemConfig.type) {
						case 'sender': {
							if (!itemConfig.disabled)
								inputField = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormSenderCombo', itemConfig);
						} break;

						case 'template': {
							if (!itemConfig.disabled)
								inputField = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormTemplateCombo', itemConfig);
						} break;
					}

					this.inputFields.push(inputField);
					this.delegate.inputFields[itemIndex] = inputField; // Delegate inputFields buffer array
				}
			}

			Ext.apply(this, {
				items: this.inputFields
			});

			this.callParent(arguments);
		}
	});

})();