(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.user.User', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.user.User'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.UserAndGroup}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupUserAbortButtonClick',
			'onUserAndGroupUserAccordionSelect = onUserAndGroupAccordionSelect',
			'onUserAndGroupUserAddButtonClick',
			'onUserAndGroupUserChangePasswordButtonClick',
			'onUserAndGroupUserDisableButtonClick',
			'onUserAndGroupUserModifyButtonClick = onUserAndGroupUserItemDoubleClick',
			'onUserAndGroupUserPrivilegedChange',
			'onUserAndGroupUserRowSelected',
			'onUserAndGroupUserSaveButtonClick',
			'onUserAndGroupUserServiceChange',
			'onUserAndGroupUserShow'
		],

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.userAndGroup.user.User}
		 *
		 * @private
		 */
		selectedUser: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.user.UserView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.user.UserAndGroup} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.user.UserView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupUserAbortButtonClick: function () {
			if (!this.userAndGroupUserSelectedUserIsEmpty()) {
				this.onUserAndGroupUserRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		/**
		 * Empty function to avoid warning
		 */
		onUserAndGroupUserAccordionSelect: Ext.emptyFn,

		/**
		 * @returns {Void}
		 */
		onUserAndGroupUserAddButtonClick: function () {
			this.grid.getSelectionModel().deselectAll();

			this.userAndGroupUserSelectedUserReset();

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.defaultGroupCombo.setDisabled(true);
			this.form.loadRecord(Ext.create('CMDBuild.model.userAndGroup.user.User'));
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupUserChangePasswordButtonClick: function () {
			this.form.setDisabledFieldSet(this.form.userPasswordFieldSet, false);

			this.form.setDisabledTopBar(true);
			this.form.setDisabledBottomBar(false);
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupUserDisableButtonClick: function () {
			var params = {};
			params['userid'] = this.userAndGroupUserSelectedUserGet('userid');
			params[CMDBuild.core.constants.Proxy.DISABLE] = this.userAndGroupUserSelectedUserGet(CMDBuild.core.constants.Proxy.IS_ACTIVE);

			CMDBuild.proxy.userAndGroup.user.User.disable({
				params: params,
				scope: this,
				success: this.success
			});
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupUserModifyButtonClick: function () {
			this.form.setDisabledFieldSet(this.form.userInfoFieldSet, false);
			this.form.setDisabledTopBar(true);
			this.form.setDisabledBottomBar(false);
		},

		/**
		 * Privileged is a specialization of service, so if someone check privileged is implicit that is a service user
		 *
		 * @returns {Void}
		 */
		onUserAndGroupUserPrivilegedChange: function () {
			if (this.form.privilegedCheckbox.getValue())
				this.form.serviceCheckbox.setValue(true);
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupUserRowSelected: function () {
			if (this.grid.getSelectionModel().hasSelection()) {
				this.userAndGroupUserSelectedUserSet({ value: this.grid.getSelectionModel().getSelection()[0] });

				this.form.reset();
				this.form.setDisabledModify(true, true);

				// Update toggleEnableDisableButton button
				this.form.toggleEnableDisableButton.setActiveState(this.userAndGroupUserSelectedUserGet(CMDBuild.core.constants.Proxy.IS_ACTIVE));

				var params = {};
				params['userid'] = this.userAndGroupUserSelectedUserGet('userid');

				this.form.defaultGroupCombo.getStore().load({
					params: params,
					scope: this,
					callback: function (records, operation, success) {
						var defaultGroup = this.form.defaultGroupCombo.getStore().findRecord('isdefault', true);

						if (!Ext.isEmpty(defaultGroup))
							this.userAndGroupUserSelectedUserSet({
								propertyName: 'defaultgroup',
								value: defaultGroup.get(CMDBuild.core.constants.Proxy.ID)
							});

						this.form.getForm().loadRecord(this.userAndGroupUserSelectedUserGet());
					}
				});
			}
		},

		/**
		 * @returns {Void}
		 *
		 * TODO: waiting for a refactor (new CRUD standards)
		 */
		onUserAndGroupUserSaveButtonClick: function () {
			if (this.validate(this.form)) { // Validate before save
				var params = this.form.getData(true);

				if (Ext.isEmpty(params['userid'])) {
					params['userid'] = -1;

					CMDBuild.proxy.userAndGroup.user.User.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.userAndGroup.user.User.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * Privileged is a specialization of service, so if someone uncheck service is implicit that is not a privileged user
		 *
		 * @returns {Void}
		 */
		onUserAndGroupUserServiceChange: function () {
			if (!this.form.serviceCheckbox.getValue())
				this.form.privilegedCheckbox.setValue(false);
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupUserShow: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = this.view.includeUnactiveUsers.getValue();

			this.grid.getStore().load({
				params: params,
				scope: this,
				callback: function (records, operation, success) {
					if (!this.grid.getSelectionModel().hasSelection())
						this.grid.getSelectionModel().select(0, true);
				}
			});
		},

		// SelectedUser property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			userAndGroupUserSelectedUserGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedUser';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			userAndGroupUserSelectedUserIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedUser';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			userAndGroupUserSelectedUserReset: function () {
				this.propertyManageReset('selectedUser');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			userAndGroupUserSelectedUserSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.userAndGroup.user.User';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedUser';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		success: function (response, options, decodedResponse) {
			decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ROWS];

			if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
				this.view.includeUnactiveUsers.reset(); // Reset checkbox value to load all users on save

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = this.view.includeUnactiveUsers.getValue();

				this.grid.getStore().load({
					params: params,
					scope: this,
					callback: function (records, operation, success) {
						var rowIndex = this.grid.getStore().find('userid', decodedResponse['userid']);

						this.grid.getSelectionModel().select(rowIndex, true);
						this.form.setDisabledModify(true);
					}
				});
			} else {
				_error('empty or unmanaged server response (' + decodedResponse + ')', this);
			}
		}
	});

})();
