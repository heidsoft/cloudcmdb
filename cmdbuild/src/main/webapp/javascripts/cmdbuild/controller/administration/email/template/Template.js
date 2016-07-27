(function() {

	Ext.define('CMDBuild.controller.administration.email.template.Template', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.email.Template'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'emailTemplateValuesDataGet',
			'emailTemplateValuesDataSet',
			'onEmailTemplateAbortButtonClick',
			'onEmailTemplateAddButtonClick',
			'onEmailTemplateModifyButtonClick = onEmailTemplateItemDoubleClick',
			'onEmailTemplateRemoveButtonClick',
			'onEmailTemplateRowSelected',
			'onEmailTemplateSaveButtonClick',
			'onEmailTemplateShow',
			'onEmailTemplateValuesButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.email.template.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.template.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.email.template.SelectedTemplate}
		 *
		 * @private
		 */
		selectedTemplate: undefined,

		/**
		 * Values windows grid data
		 *
		 * @property {Array}
		 *
		 * @private
		 */
		valuesData: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.template.TemplateView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.template.TemplateView} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.template.TemplateView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
			this.form = this.view.form;
		},

		// Values data property methods
			/**
			 * @return {Object}
			 */
			emailTemplateValuesDataGet: function() {
				return this.valuesData;
			},

			/**
			 * @param {Object} dataObject
			 *
			 * @private
			 */
			emailTemplateValuesDataReset: function() {
				this.valuesData = null;
			},

			/**
			 * @param {Object} dataObject
			 */
			emailTemplateValuesDataSet: function(dataObject) {
				this.valuesData = dataObject || {};
			},

		onEmailTemplateAbortButtonClick: function() {
			if (!this.emailTemplateSelectedTemplateIsEmpty()) {
				this.cmfg('onEmailTemplateRowSelected');
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onEmailTemplateAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.emailTemplateSelectedTemplateReset();
			this.emailTemplateValuesDataReset();

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.email.template.SelectedTemplate'));
		},

		onEmailTemplateModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onEmailTemplateRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function(buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onEmailTemplateRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.email.Template.read({
					params: params,
					scope: this,
					failure: function(response, options, decodedResponse) {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.common.failure,
							Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure, this.emailTemplateSelectedTemplateGet(CMDBuild.core.constants.Proxy.NAME)),
							false
						);
					},
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						this.emailTemplateSelectedTemplateSet({ value: decodedResponse });

						this.cmfg('emailTemplateValuesDataSet', this.emailTemplateSelectedTemplateGet(CMDBuild.core.constants.Proxy.VARIABLES));

						this.form.loadRecord(this.emailTemplateSelectedTemplateGet());
						this.form.setDisabledModify(true, true);
					}
				});
			}
		},

		onEmailTemplateSaveButtonClick: function() {
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				// To put and encode variablesWindow grid values
				formData[CMDBuild.core.constants.Proxy.VARIABLES] = Ext.encode(this.cmfg('emailTemplateValuesDataGet'));

				if (Ext.isEmpty(formData.id)) {
					CMDBuild.proxy.email.Template.create({
						params: formData,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.email.Template.update({
						params: formData,
						scope: this,
						success: this.success
					});
				}
			}
		},

		onEmailTemplateShow: function() {
			this.grid.getStore().load({
				scope: this,
				callback: function(records, operation, success) {
					if (!this.grid.getSelectionModel().hasSelection())
						this.grid.getSelectionModel().select(0, true);
				}
			});
		},

		onEmailTemplateValuesButtonClick: function() {
			Ext.create('CMDBuild.controller.administration.email.template.Values', { parentDelegate: this });
		},

		removeItem: function() {
			if (!this.emailTemplateSelectedTemplateIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.emailTemplateSelectedTemplateGet(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.email.Template.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function(records, operation, success) {
								this.grid.getSelectionModel().select(0, true);

								if (!this.grid.getSelectionModel().hasSelection())
									this.form.setDisabledModify(true, true, true);
							}
						});
					}
				});
			}
		},

		// SelectedAccount property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 *
			 * @private
			 */
			emailTemplateSelectedTemplateGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTemplate';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 *
			 * @private
			 */
			emailTemplateSelectedTemplateIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTemplate';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @private
			 */
			emailTemplateSelectedTemplateReset: function() {
				this.propertyManageReset('selectedTemplate');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			emailTemplateSelectedTemplateSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.email.template.SelectedTemplate';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTemplate';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			this.grid.getStore().load({
				scope: this,
				callback: function(records, operation, success) {
					var rowIndex = this.grid.getStore().find(
						CMDBuild.core.constants.Proxy.NAME,
						this.form.getForm().findField(CMDBuild.core.constants.Proxy.NAME).getValue()
					);

					this.grid.getSelectionModel().select(rowIndex, true);
					this.cmfg('onEmailTemplateRowSelected');
				}
			});
		}
	});

})();