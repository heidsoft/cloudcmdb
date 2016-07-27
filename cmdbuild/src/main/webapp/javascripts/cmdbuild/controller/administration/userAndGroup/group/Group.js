(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.Group', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.userAndGroup.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.UserAndGroup}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.DefaultFilters}
		 */
		controllerDefaultFilters: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges}
		 */
		controllerPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.UserInterface}
		 */
		controllerUserInterface: undefined,

		/**
		 * @property {CMDBuild.controller.administration.userAndGroup.group.Users}
		 */
		controllerUsers: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupAccordionSelect = onUserAndGroupAccordionSelect',
			'onUserAndGroupGroupAddButtonClick',
			'onUserAndGroupGroupSelected -> controllerProperties, controllerPrivileges, controllerUsers, controllerUserInterface, controllerDefaultFilters',
			'onUserAndGroupGroupSetActiveTab',
			'userAndGroupGroupSelectedGroupGet',
			'userAndGroupGroupSelectedGroupIsEmpty',
			'userAndGroupGroupSelectedGroupReset',
			'userAndGroupGroupSelectedGroupSet'
		],

		/**
		 * @property {CMDBuild.model.lookup.Type} or null
		 *
		 * @private
		 */
		selectedGroup: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.GroupView}
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

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.GroupView', { delegate: this });

			this.view.tabPanel.removeAll();

			// Controller build
			this.controllerDefaultFilters = Ext.create('CMDBuild.controller.administration.userAndGroup.group.DefaultFilters', { parentDelegate: this });
			this.controllerPrivileges = Ext.create('CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges', { parentDelegate: this });
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.userAndGroup.group.Properties', { parentDelegate: this });
			this.controllerUserInterface = Ext.create('CMDBuild.controller.administration.userAndGroup.group.UserInterface', { parentDelegate: this });
			this.controllerUsers = Ext.create('CMDBuild.controller.administration.userAndGroup.group.Users', { parentDelegate: this });

			// Inject tabs (sorted)
			this.view.tabPanel.add(this.controllerProperties.getView());
			this.view.tabPanel.add(this.controllerPrivileges.getView());
			this.view.tabPanel.add(this.controllerUsers.getView());
			this.view.tabPanel.add(this.controllerUserInterface.getView());
			this.view.tabPanel.add(this.controllerDefaultFilters.getView());

			this.onUserAndGroupGroupSetActiveTab();
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupAccordionSelect: function () {
			if (!this.cmfg('userAndGroupSelectedAccordionIsEmpty'))
				CMDBuild.proxy.userAndGroup.group.Group.read({ // TODO: waiting for refactor (crud)
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.GROUPS];

						var selectedGroupModel = Ext.Array.findBy(decodedResponse, function (groupObject, i) {
							return this.cmfg('userAndGroupSelectedAccordionGet', CMDBuild.core.constants.Proxy.ID) == groupObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedGroupModel)) {
							this.cmfg('userAndGroupGroupSelectedGroupSet', { value: selectedGroupModel });
							this.cmfg('onUserAndGroupGroupSelected');

							if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
								this.onUserAndGroupGroupSetActiveTab();

							this.view.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
						}
					}
				});
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupAddButtonClick: function () {
			this.cmfg('mainViewportAccordionDeselect', CMDBuild.core.constants.ModuleIdentifiers.getUserAndGroup());

			// Forwarding
			this.controllerDefaultFilters.cmfg('onUserAndGroupGroupTabDefaultFiltersAddButtonClick');
			this.controllerPrivileges.cmfg('onUserAndGroupGroupTabPrivilegesAddButtonClick');
			this.controllerProperties.cmfg('onUserAndGroupGroupTabPropertiesAddButtonClick');
			this.controllerUserInterface.cmfg('onUserAndGroupGroupTabUserInterfaceAddButtonClick');
			this.controllerUsers.cmfg('onUserAndGroupGroupTabUsersAddButtonClick');
		},

		/**
		 * @param {Number} index
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupSetActiveTab: function (index) {
			this.view.tabPanel.setActiveTab(index || 0);
		},

		// SelectedGroup property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			userAndGroupGroupSelectedGroupGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedGroup';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			userAndGroupGroupSelectedGroupIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedGroup';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @returns {Void}
			 */
			userAndGroupGroupSelectedGroupReset: function () {
				this.propertyManageReset('selectedGroup');
			},

			/**
			 * @property {Object} parameters
			 *
			 * @returns {Void}
			 */
			userAndGroupGroupSelectedGroupSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.userAndGroup.group.Group';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedGroup';

					this.propertyManageSet(parameters);
				}
			}
	});

})();
