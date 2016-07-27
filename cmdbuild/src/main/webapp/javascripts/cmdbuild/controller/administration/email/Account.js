(function () {

	Ext.define('CMDBuild.controller.administration.email.Account', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.email.Account'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEmailAccountAbortButtonClick',
			'onEmailAccountAddButtonClick',
			'onEmailAccountModifyButtonClick = onEmailAccountItemDoubleClick',
			'onEmailAccountRemoveButtonClick',
			'onEmailAccountRowSelected',
			'onEmailAccountSaveButtonClick',
			'onEmailAccountSetDefaultButtonClick',
			'onEmailAccountShow'
		],

		/**
		 * @property {CMDBuild.view.administration.email.account.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.account.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.email.account.SelectedAccount}
		 *
		 * @private
		 */
		selectedAccount: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.account.AccountView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.email.Email} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.account.AccountView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onEmailAccountAbortButtonClick: function () {
			if (!this.emailAccountSelectedAccountIsEmpty()) {
				this.cmfg('onEmailAccountRowSelected');
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onEmailAccountAddButtonClick: function () {
			this.grid.getSelectionModel().deselectAll();

			this.emailAccountSelectedAccountReset();

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.email.account.SelectedAccount'));
		},

		onEmailAccountModifyButtonClick: function () {
			this.form.setDisabledModify(false);
		},

		onEmailAccountRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onEmailAccountRowSelected: function () {
			if (this.grid.getSelectionModel().hasSelection()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.email.Account.read({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (!Ext.isEmpty(decodedResponse)) {
							this.emailAccountSelectedAccountSet({ value: decodedResponse });

							this.form.loadRecord(this.emailAccountSelectedAccountGet());
							this.form.setDisabledModify(true, true);

							// Set disable state to setDefaultButton and removeButton related to selectedAccount
							this.form.removeButton.setDisabled(this.emailAccountSelectedAccountGet(CMDBuild.core.constants.Proxy.IS_DEFAULT));
							this.form.setDefaultButton.setDisabled(this.emailAccountSelectedAccountGet(CMDBuild.core.constants.Proxy.IS_DEFAULT));
						}
					}
				});
			}
		},

		onEmailAccountSaveButtonClick: function () {
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				if (Ext.isEmpty(formData.id)) {
					CMDBuild.proxy.email.Account.create({
						params: formData,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.email.Account.update({
						params: formData,
						scope: this,
						success: this.success
					});
				}
			}
		},

		onEmailAccountSetDefaultButtonClick: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.NAME] = this.emailAccountSelectedAccountGet(CMDBuild.core.constants.Proxy.NAME);

			CMDBuild.proxy.email.Account.setDefault({
				params: params,
				scope: this,
				success: this.success
			});
		},

		onEmailAccountShow: function () {
			this.grid.getStore().load({
				scope: this,
				callback: function (records, operation, success) {
					if (!this.grid.getSelectionModel().hasSelection())
						this.grid.getSelectionModel().select(0, true);
				}
			});
		},

		/**
		 * @private
		 */
		removeItem: function () {
			if (!this.emailAccountSelectedAccountIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.emailAccountSelectedAccountGet(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.email.Account.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function (records, operation, success) {
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
			emailAccountSelectedAccountGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccount';
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
			emailAccountSelectedAccountIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccount';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @private
			 */
			emailAccountSelectedAccountReset: function () {
				this.propertyManageReset('selectedAccount');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			emailAccountSelectedAccountSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.email.account.SelectedAccount';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccount';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @private
		 */
		success: function (response, options, decodedResponse) {
			this.grid.getStore().load({
				scope: this,
				callback: function (records, operation, success) {
					var rowIndex = this.grid.getStore().find(
						CMDBuild.core.constants.Proxy.NAME,
						this.form.getForm().findField(CMDBuild.core.constants.Proxy.NAME).getValue()
					);

					this.grid.getSelectionModel().select(rowIndex, true);
					this.cmfg('onEmailAccountRowSelected');
				}
			});
		}
	});

})();
