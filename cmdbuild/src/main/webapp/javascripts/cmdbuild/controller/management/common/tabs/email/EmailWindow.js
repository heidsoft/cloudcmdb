(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.controller.common.abstract.Widget',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.common.tabs.email.Attachment',
			'CMDBuild.proxy.email.Template',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.tabs.email.attachments.Attachments}
		 */
		attachmentsDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getView = tabEmailEmailWindowGetView',
			'onTabEmailEmailWindowAbortButtonClick',
			'onTabEmailEmailWindowBeforeDestroy',
			'onTabEmailEmailWindowConfirmButtonClick',
			'onTabEmailEmailWindowFieldChange',
			'onTabEmailEmailWindowFillFromTemplateButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.emailWindow.EditForm}
		 */
		form: undefined,

		/**
		 * Used as flag to avoid pop-up spam
		 *
		 * @cfg {Boolean}
		 */
		isAdvicePrompted: false,

		/**
		 * @cfg {CMDBuild.model.common.tabs.email.Email}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {Mixed} emailWindows
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		windowMode: 'create',

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		windowModeAvailable: ['create', 'edit', 'reply', 'view'],

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.Grid} configurationObject.parentDelegate
		 * @param {Mixed} configurationObject.record
		 * @param {String} configurationObject.windowMode
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			var windowClassName = null;

			this.callParent(arguments);

			if (Ext.Array.contains(this.windowModeAvailable, this.windowMode)) {
				switch (this.windowMode) {
					case 'view': {
						windowClassName = 'CMDBuild.view.management.common.tabs.email.emailWindow.ViewWindow';
					} break;

					default: { // Default window class to build
						windowClassName = 'CMDBuild.view.management.common.tabs.email.emailWindow.EditWindow';
					}
				}

				this.view = Ext.create(windowClassName, { delegate: this });

				// Shorthands
				this.form = this.view.form;

				// Fill from template button store configuration
				CMDBuild.proxy.email.Template.readAll({
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.ELEMENTS];

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							// Sort templates by description ascending
							CMDBuild.core.Utils.objectArraySort(decodedResponse, CMDBuild.core.constants.Proxy.DESCRIPTION);

							Ext.Array.forEach(decodedResponse, function (template, i, allTemplates) {
								this.view.fillFromTemplateButton.menu.add({
									text: template[CMDBuild.core.constants.Proxy.DESCRIPTION],
									templateName: template[CMDBuild.core.constants.Proxy.NAME],
									scope: this,

									handler: function (button, e) {
										this.cmfg('onTabEmailEmailWindowFillFromTemplateButtonClick', button[CMDBuild.core.constants.Proxy.TEMPLATE_NAME]);
									}
								});
							}, this);
						} else { // To disable button if the aren't templates
							this.view.fillFromTemplateButton.setDisabled(true);
						}
					}
				});

				if (CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ENABLED)) {
					// Build attachments controller
					this.attachmentsDelegate = Ext.create('CMDBuild.controller.management.common.tabs.email.attachments.Attachments', {
						parentDelegate: this,
						record: this.record,
						view: this.view.attachmentContainer
					});

					// Get all email attachments
					var params = {};
					params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
					params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);

					CMDBuild.proxy.common.tabs.email.Attachment.readAll({
						params: params,
						loadMask: this.view,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse))
								Ext.Array.forEach(decodedResponse, function (attachmentObject, i, allAttachmentObjects) {
									if(!Ext.Object.isEmpty(attachmentObject))
										this.attachmentsDelegate.cmfg('tabEmailAttachmentAddPanel', attachmentObject[CMDBuild.core.constants.Proxy.FILE_NAME]);
								}, this);
						}
					});
				}

				this.form.loadRecord(this.record); // Fill view form with record data

				// If email has template enable keep-synch checkbox
				if (!Ext.isEmpty(this.record.get(CMDBuild.core.constants.Proxy.TEMPLATE)) && this.windowMode != 'view')
					this.form.keepSynchronizationCheckbox.setDisabled(false);

				// Show window
				if (!Ext.isEmpty(this.view))
					this.view.show();
			}
		},

		/**
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isKeepSynchronizationChecked: function () {
			return this.form.keepSynchronizationCheckbox.getValue();
		},

		/**
		 * @param {CMDBuild.model.common.tabs.email.Template} record
		 *
		 * @private
		 */
		loadFormValues: function (record) {
			if (!Ext.isEmpty(record)) {
				var xaVars = Ext.apply({}, record.getData(), record.get(CMDBuild.core.constants.Proxy.VARIABLES));

				this.templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.cmfg('tabEmailGetFormForTemplateResolver'),
					xaVars: xaVars,
					serverVars: CMDBuild.controller.common.abstract.Widget.getTemplateResolverServerVars(
						this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ENTITY)
					)
				});

				this.templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					scope: this,
					callback: function (values, ctx) {
						var setValueArray = [];
						var content = values[CMDBuild.core.constants.Proxy.BODY];

						if (this.windowMode == 'reply') {
							setValueArray.push({
								id: CMDBuild.core.constants.Proxy.BODY,
								value: content + '<br /><br />' + this.record.get(CMDBuild.core.constants.Proxy.BODY)
							});
						} else {
							setValueArray.push(
								{
									id: CMDBuild.core.constants.Proxy.FROM,
									value: values[CMDBuild.core.constants.Proxy.FROM]
								},
								{
									id: CMDBuild.core.constants.Proxy.TO,
									value: values[CMDBuild.core.constants.Proxy.TO]
								},
								{
									id: CMDBuild.core.constants.Proxy.CC,
									value: values[CMDBuild.core.constants.Proxy.CC]
								},
								{
									id: CMDBuild.core.constants.Proxy.BCC,
									value: values[CMDBuild.core.constants.Proxy.BCC]
								},
								{
									id: CMDBuild.core.constants.Proxy.SUBJECT,
									value: values[CMDBuild.core.constants.Proxy.SUBJECT]
								},
								{
									id: CMDBuild.core.constants.Proxy.BODY,
									value: content
								},
								{ // It's last one to avoid Notification pop-up display on setValues action
									id: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION,
									value: values[CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION]
								}
							);
						}

						this.form.getForm().setValues(setValueArray);

						this.form.delayField.setValue(values[CMDBuild.core.constants.Proxy.DELAY]);

						// Updates record's prompt synchronizations flag
						this.record.set(CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION, values[CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION]);
					}
				});
			} else {
				_error('empty record parameter for loadFormValues()', this);
			}
		},

		/**
		 * Destroy email window object
		 */
		onTabEmailEmailWindowAbortButtonClick: function () {
			this.cmfg('onTabEmailEmailWindowBeforeDestroy');

			if (!Ext.isEmpty(this.view))
				this.view.destroy();
		},

		/**
		 * Implements empty email deletion on window destroy
		 */
		onTabEmailEmailWindowBeforeDestroy: function () {
			if (Ext.isFunction(this.form.getData) && CMDBuild.core.Utils.isObjectEmpty(this.form.getData()))
				this.cmfg('tabEmailGridRecordRemove', this.record);
		},

		/**
		 * Updates record object adding id (time in milliseconds), Description and attachments array and adds email record to grid store
		 */
		onTabEmailEmailWindowConfirmButtonClick: function () {
			// Validate before save
			if (this.validate(this.form)) {
				var formValues = this.form.getForm().getValues();

				// Apply formValues to record object
				for (var key in formValues)
					this.record.set(key, formValues[key]);

				// Setup attachments only if DMS is enabled
				if (CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ENABLED))
					this.record.set(CMDBuild.core.constants.Proxy.ATTACHMENTS, this.attachmentsDelegate.cmfg('tabEmailAttachmentNamesGet'));

				this.record.set(CMDBuild.core.constants.Proxy.REFERENCE, this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID));

				if (Ext.isEmpty(this.record.get(CMDBuild.core.constants.Proxy.ID))) {
					this.cmfg('tabEmailGridRecordAdd', { record: this.record });
				} else {
					this.cmfg('tabEmailGridRecordEdit', { record: this.record });
				}

				if (!Ext.isEmpty(this.templateResolver))
					this.cmfg('tabEmailBindLocalDepsChangeEvent',{
						record: this.record,
						templateResolver: this.templateResolver
					});

				this.cmfg('onTabEmailEmailWindowAbortButtonClick');
			}
		},

		/**
		 * Change event management to catch email content edit
		 */
		onTabEmailEmailWindowFieldChange: function () {
			if (!this.isAdvicePrompted && this.isKeepSynchronizationChecked()) {
				this.isAdvicePrompted = true;

				CMDBuild.core.Message.warning(null, CMDBuild.Translation.errors.emailChangedWithAutoSynch);
			}
		},

		/**
		 * Using FillFromTemplateButton I put only tempalteName in emailObject and get template data to fill email form
		 *
		 * @param {String} templateName
		 */
		onTabEmailEmailWindowFillFromTemplateButtonClick: function (templateName) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.NAME] = templateName;

			CMDBuild.proxy.email.Template.read({
				params: params,
				scope: this,
				loadMask: true,
				failure: function (response, options, decodedResponse) {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure),
						false
					);
				},
				success: function (response, options, decodedResponse) {
					var response = decodedResponse.response;

					this.loadFormValues(Ext.create('CMDBuild.model.common.tabs.email.Template', response));

					// Bind extra form fields to email record
					this.record.set(CMDBuild.core.constants.Proxy.TEMPLATE, response[CMDBuild.core.constants.Proxy.NAME]);
					this.record.set(CMDBuild.core.constants.Proxy.ACCOUNT, response[CMDBuild.core.constants.Proxy.DEFAULT_ACCOUNT]);

					this.form.keepSynchronizationCheckbox.setDisabled(false);
				}
			});
		}
	});

})();
