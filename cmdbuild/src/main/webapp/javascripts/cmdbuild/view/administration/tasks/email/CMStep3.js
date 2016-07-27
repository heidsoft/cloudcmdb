(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.configuration.Dms',
			'CMDBuild.proxy.lookup.Lookup'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormEmailController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.email.CMStep3}
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
				case 'onCheckedAttachmentsFieldset':
					return this.onCheckedAttachmentsFieldset();

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
			 * @return {Boolean}
			 */
			getValueAttachmentsFieldsetCheckbox: function() {
				return this.view.attachmentsFieldset.checkboxCmp.getValue();
			},

			/**
			 * @return {Boolean}
			 */
			getValueNotificationFieldsetCheckbox: function() {
				return this.view.notificationFieldset.checkboxCmp.getValue();
			},

			/**
			 * @return {Boolean}
			 */
			getValueParsingFieldsetCheckbox: function() {
				return this.view.parsingFieldset.checkboxCmp.getValue();
			},

		/**
		 * Read CMDBuild's alfresco configuration from server and set Combobox store
		 */
		onCheckedAttachmentsFieldset: function() {
			var me = this;

			if (this.view.attachmentsCombo.store.getCount() == 0)
				CMDBuild.proxy.configuration.Dms.read({
					success: function(response) {
						var decodedJson = Ext.JSON.decode(response.responseText);
						var params = {};
						params[CMDBuild.core.constants.Proxy.TYPE] = decodedJson.data['category.lookup'];
						params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
						params[CMDBuild.core.constants.Proxy.SHORT] = true;

						me.view.attachmentsCombo.getStore().load({ params: params });
					}
				});
		},

		// SETters functions
			/**
			 * Set attachments field as required/unrequired
			 *
			 * @param {Boolean} state
			 */
			setAllowBlankAttachmentsField: function(state) {
				this.view.attachmentsCombo.allowBlank = state;
			},

			/**
			 * Set parsing fields as required/unrequired
			 *
			 * @param {Boolean} state
			 */
			setAllowBlankParsingFields: function(state) {
				this.view.parsingKeyStart.allowBlank = state;
				this.view.parsingKeyEnd.allowBlank = state;
				this.view.parsingValueStart.allowBlank = state;
				this.view.parsingValueEnd.allowBlank = state;
				this.view.parsingFieldset.allowBlank = state;
			},

			/**
			 * @param {String} value
			 */
			setValueAttachmentsCombo: function(value) {
				this.view.attachmentsCombo.setValue(value);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueAttachmentsFieldsetCheckbox: function(state) {
				if (state) {
					this.view.attachmentsFieldset.expand();
					this.onCheckedAttachmentsFieldset();
				} else {
					this.view.attachmentsFieldset.collapse();
				}
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
			 * Setup all parsing fieldset input values
			 *
			 * @param {String} keyInit
			 * @param {String} keyEnd
			 * @param {String} valueInit
			 * @param {String} valueEnd
			 */
			setValueParsingFields: function(keyInit, keyEnd, valueInit, valueEnd) {
				this.view.parsingKeyStart.setValue(keyInit);
				this.view.parsingKeyEnd.setValue(keyEnd);
				this.view.parsingValueStart.setValue(valueInit);
				this.view.parsingValueEnd.setValue(valueEnd);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueParsingFieldsetCheckbox: function(state) {
				if (state) {
					this.view.parsingFieldset.expand();
				} else {
					this.view.parsingFieldset.collapse();
				}
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.lookup.Lookup'
		],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.email.CMStep3Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		attachmentsCombo: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		attachmentsFieldset: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		notificationFieldset: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm}
		 */
		notificationForm: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		parsingFieldset: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		parsingKeyEnd: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		parsingKeyStart: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		parsingValueEnd: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		parsingValueStart: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep3Delegate', this);

			// Parsing configuration
				this.parsingKeyStart = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingKeyStart,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					name: CMDBuild.core.constants.Proxy.PARSING_KEY_INIT,
					flex: 1
				});

				this.parsingKeyEnd = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingKeyEnd,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					name: CMDBuild.core.constants.Proxy.PARSING_KEY_END,
					margin: '0 0 0 20',
					flex: 1
				});

				this.parsingValueStart = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingValueStart,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					name: CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT,
					flex: 1
				});

				this.parsingValueEnd = Ext.create('Ext.form.field.Text', {
					fieldLabel: tr.parsingValueEnd,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					name: CMDBuild.core.constants.Proxy.PARSING_VALUE_END,
					margin: '0 0 0 20',
					flex: 1
				});

				this.parsingFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.bodyParsing,
					checkboxName: CMDBuild.core.constants.Proxy.PARSING_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,
					overflowY: 'auto',

					items: [
						{
							xtype: 'container',

							layout: {
								type: 'hbox',
								align:'stretch'
							},

							items: [this.parsingKeyStart, this.parsingKeyEnd]
						},
						{
							xtype: 'container',
							margin: '10 0',

							layout: {
								type: 'hbox',
								align:'stretch'
							},

							items: [this.parsingValueStart, this.parsingValueEnd]
						}
					]
				});

				this.parsingFieldset.fieldWidthsFix();
			// END: BodyParsing configuration

			// Email notification configuration
				this.notificationForm = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
					template: {
						type: 'template',
						disabled: false
					}
				});

				this.notificationFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.administration.tasks.notificationForm.title,
					checkboxName: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,
					overflowY: 'auto',

					items: [this.notificationForm]
				});

				this.notificationFieldset.fieldWidthsFix();
			// END: Email notification configuration

			// Attachments configuration
				this.attachmentsCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY,
					fieldLabel: tr.attachmentsCategory,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					anchor: '100%',
					displayField: 'Description',
					valueField: 'Id',
					forceSelection: true,
					editable: false,

					store: CMDBuild.proxy.lookup.Lookup.getStore(),
					queryMode: 'local'
				});

				this.attachmentsFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.saveToAlfresco,
					checkboxName: CMDBuild.core.constants.Proxy.ATTACHMENTS_ACTIVE,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,
					overflowY: 'auto',

					items: [this.attachmentsCombo],

					listeners: {
						expand: function(fieldset, eOpts) {
							me.delegate.cmOn('onCheckedAttachmentsFieldset');
						}
					}
				});

				this.attachmentsFieldset.fieldWidthsFix();
			// END: Attachments configuration

			Ext.apply(this, {
				items: [
					this.parsingFieldset,
					this.notificationFieldset,
					this.attachmentsFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();