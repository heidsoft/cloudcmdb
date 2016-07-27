(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.UserInterface', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.Group',
			'CMDBuild.proxy.userAndGroup.group.UserInterface'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupTabUserInterfaceAbortButtonClick',
			'onUserAndGroupGroupTabUserInterfaceAddButtonClick',
			'onUserAndGroupGroupTabUserInterfaceGroupSelected = onUserAndGroupGroupSelected',
			'onUserAndGroupGroupTabUserInterfaceSaveButtonClick',
			'onUserAndGroupGroupTabUserInterfaceShow'
		],

		/**
		 * @property {CMDBuild.model.userAndGroup.group.userInterface.UserInterface}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.userInterface.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.userInterface.UserInterfaceView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.Group} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.userInterface.UserInterfaceView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		// Configuration property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			configurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * Loads data model and sub model data to form
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			configurationLoad: function () {
				this.form.reset();

				this.form.getForm().loadRecord(this.configurationGet());
				this.form.getForm().loadRecord(this.configurationGet(CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS));
				this.form.getForm().loadRecord(this.configurationGet(CMDBuild.core.constants.Proxy.DISABLED_MODULES));
				this.form.getForm().loadRecord(this.configurationGet(CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS));
			},

			/**
			 * @property {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			configurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.userAndGroup.group.userInterface.UserInterface';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUserInterfaceAbortButtonClick: function () {
			this.cmfg('onUserAndGroupGroupTabUserInterfaceShow');
		},

		/**
		 * Disable tab on add button click
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUserInterfaceAddButtonClick: function () {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating group privileges, CloudAdministrators couldn't change UIConfiguration of full administrator groups
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUserInterfaceGroupSelected: function () {
			CMDBuild.proxy.userAndGroup.group.Group.read({ // TODO: waiting for refactor (crud)
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.GROUPS];

					var loggedUserCurrentGroup = Ext.Array.findBy(decodedResponse, function (groupObject, i) {
						return CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_ID) == groupObject[CMDBuild.core.constants.Proxy.ID];
					}, this);

					if (!Ext.isEmpty(loggedUserCurrentGroup)) {
						this.view.setDisabled(
							this.cmfg('userAndGroupGroupSelectedGroupIsEmpty')
							|| loggedUserCurrentGroup[CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR]
							&& this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR)
							&& !this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR)
						);
					}
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUserInterfaceSaveButtonClick: function () {
			this.configurationSet({ value: this.form.getForm().getValues() });

			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.UI_CONFIGURATION] = Ext.encode(this.configurationGet().getData());

			CMDBuild.proxy.userAndGroup.group.UserInterface.update({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					CMDBuild.core.Message.success();

					this.cmfg('onUserAndGroupGroupTabUserInterfaceShow');
				}
			});
		},

		/**
		 * Loads tab data
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUserInterfaceShow: function () {
			if (!this.cmfg('userAndGroupGroupSelectedGroupIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.userAndGroup.group.UserInterface.read({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						this.configurationSet({ value: decodedResponse });

						this.configurationLoad();
					}
				});
			}
		}
	});

})();
