(function () {

	Ext.define('CMDBuild.controller.configure.Configure', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Configure'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationViewportCreditsClick',
			'onConfigurationViewportWizardConnectionCheckButtonClick',
			'onConfigurationViewportWizardDbTypeChange',
			'onConfigurationViewportWizardFinishButtonClick',
			'onConfigurationViewportWizardNavigationButtonClick',
			'onConfigurationViewportWizardPanelShow',
			'onConfigurationViewportWizardUserTypeChange'
		],

		/**
		 * @property {CMDBuild.view.configure.Wizard}
		 */
		wizard: undefined,

		/**
		 * @property {CMDBuild.view.patchManager.PatchManagerViewport}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.configure.ConfigureViewport', { delegate: this });

			// Shorthands
			this.wizard = this.view.wizard;
			this.wizardStep1 = this.view.wizard.step1;
			this.wizardStep2 = this.view.wizard.step2;
			this.wizardStep3 = this.view.wizard.step3;

			this.manageWizardButtonsDisabledState();
			this.view.wizard.loadRecord(Ext.create('CMDBuild.model.configure.Configure')); // Apply default values
		},

		/**
		 * @returns {Boolean}
		 *
		 * @private
		 */
		hasNext: function () {
			return (
				this.wizard.getLayout().getNext()
				&& !Ext.isEmpty(this.wizard.getLayout().getNext().isDisabled()) && !this.wizard.getLayout().getNext().isDisabled()
			);
		},

		/**
		 * @returns {Boolean}
		 *
		 * @private
		 */
		hasPrevious: function () {
			return (
				this.wizard.getLayout().getPrev()
				&& !Ext.isEmpty(this.wizard.getLayout().getPrev().isDisabled()) && !this.wizard.getLayout().getPrev().isDisabled()
			);
		},

		/**
		 * @private
		 */
		manageWizardButtonsDisabledState: function () {
			this.wizard.previousButton.setDisabled(!this.hasPrevious());
			this.wizard.nextButton.setDisabled(!this.hasNext());
		},

		/**
		 * Manages footer credits link click action
		 */
		onConfigurationViewportCreditsClick: function () {
			Ext.create('CMDBuild.core.window.Credits').show();
		},

		onConfigurationViewportWizardConnectionCheckButtonClick: function () {
			CMDBuild.proxy.Configure.dbConnectionCheck({
				params: Ext.create('CMDBuild.model.configure.Configure', this.wizard.getData()).getDataDBConnection(),
				scope: this,
				success: function (response, options, decodedResponse){
					Ext.Msg.show({
						title: CMDBuild.Translation.testConnection,
						msg: CMDBuild.Translation.connectionSuccessful,
						buttons: Ext.MessageBox.OK
					});
				}
			});
		},

		onConfigurationViewportWizardDbTypeChange: function () {
			var configurationRecordModel = Ext.create('CMDBuild.model.configure.Configure', this.wizard.getData());
			var defaultValuesModel = Ext.create('CMDBuild.model.configure.Configure'); // Default values

			switch (configurationRecordModel.get(CMDBuild.core.constants.Proxy.DATABASE_TYPE)) {
				case CMDBuild.core.constants.Proxy.DEMO: {
					// Buttons manage
					this.wizard.finishButton.show();
					this.wizard.nextButton.hide();

					// FieldSet manage
					this.wizard.setDisabledFieldSet(this.wizardStep2.userFieldSet, true);

					// Step2 fields manage
					this.wizardStep2.createSharkSchemaCheckbox.enable();
					this.wizardStep2.databaseUserTypeCombobox.setValue(defaultValuesModel.get(this.wizardStep2.databaseUserTypeCombobox.getName()));
					this.wizardStep2.userFieldSet.show();

					// Step3 manage
					this.wizardStep3.disable();
				} break;

				case CMDBuild.core.constants.Proxy.EMPTY: {
					// Buttons manage
					this.wizard.finishButton.hide();
					this.wizard.nextButton.show();

					// FieldSet manage
					this.wizard.setDisabledFieldSet(this.wizardStep2.userFieldSet, true);

					// Step2 fields manage
					this.wizardStep2.createSharkSchemaCheckbox.enable();
					this.wizardStep2.databaseUserTypeCombobox.setValue(defaultValuesModel.get(this.wizardStep2.databaseUserTypeCombobox.getName()));
					this.wizardStep2.userFieldSet.show();

					// Step3 manage
					this.wizardStep3.enable();
				} break;

				case CMDBuild.core.constants.Proxy.EXISTING: {
					// Buttons manage
					this.wizard.finishButton.show();
					this.wizard.nextButton.hide();

					// FieldSet manage
					this.wizard.setDisabledFieldSet(this.wizardStep2.userFieldSet, true);

					// Step2 fields manage
					this.wizardStep2.createSharkSchemaCheckbox.disable();
					this.wizardStep2.createSharkSchemaCheckbox.setValue(false);
					this.wizardStep2.userFieldSet.hide();

					// Step3 manage
					this.wizardStep3.disable();
				} break;

				default: {
					_error('unmanaged DB Type selection "' + configurationRecordModel.get(CMDBuild.core.constants.Proxy.DATABASE_TYPE) + '"', this);
				}
			}
		},

		onConfigurationViewportWizardFinishButtonClick: function () {
			if (this.validate(this.wizard)) {
				CMDBuild.proxy.Configure.apply({
					params: Ext.create('CMDBuild.model.configure.Configure', this.wizard.getData()).getDataSubmit(),
					scope: this,
					success: function (response, options, decodedResponse) {
						Ext.Msg.show({
							title: CMDBuild.Translation.configurationDone,
							msg: CMDBuild.Translation.configurationWizardSuccessMessage,
							buttons: Ext.MessageBox.OK,

							fn: function (buttonId, text, opt) {
								window.location = 'administration.jsp';
							}
						});
					}
				});
			}
		},

		/**
		 * To change wizard displayed item
		 *
		 * @param (String) action
		 */
		onConfigurationViewportWizardNavigationButtonClick: function (action) {
			switch (action) {
				case 'next': {
					if (this.hasNext())
						this.wizard.getLayout().next();
				} break;

				case 'previous': {
					if (this.hasPrevious())
						this.wizard.getLayout().prev();
				} break;
			}

			this.manageWizardButtonsDisabledState();

			// Fires show event on first item
			if (!Ext.isEmpty(this.wizard.getLayout().getActiveItem()) && !this.wizard.getLayout().getPrev())
				this.wizard.getLayout().getActiveItem().fireEvent('show');
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.displayFinishButton
		 * @param {Boolean} parameters.displayNextButton
		 * @param {Boolean} parameters.displayPreviusButton
		 */
		onConfigurationViewportWizardPanelShow: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			Ext.applyIf(parameters, {
				displayFinishButton: false,
				displayNextButton: false,
				displayPreviusButton: false
			});

			this.wizard.previousButton.setVisible(parameters.displayPreviusButton);
			this.wizard.nextButton.setVisible(parameters.displayNextButton);
			this.wizard.finishButton.setVisible(parameters.displayFinishButton);
		},

		onConfigurationViewportWizardUserTypeChange: function () {
			this.wizard.setDisabledFieldSet(this.wizardStep2.userFieldSet, true);

			switch (this.wizardStep2.databaseUserTypeCombobox.getValue()) {
				case 'new_limuser': {
					this.wizardStep2.databaseUserNameField.enable();
					this.wizardStep2.databaseUserPasswordConfirmationField.enable();
					this.wizardStep2.databaseUserPasswordField.enable();
				} break;

				case 'limuser': {
					this.wizardStep2.databaseUserNameField.enable();
					this.wizardStep2.databaseUserPasswordField.enable();
				} break;
			}
		}
	});

})();
