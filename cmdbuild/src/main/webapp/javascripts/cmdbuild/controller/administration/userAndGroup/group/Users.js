(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.Users', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupTabUsersAddButtonClick',
			'onUserAndGroupGroupTabUsersGroupSelected = onUserAndGroupGroupSelected',
			'onUserAndGroupGroupTabUsersSaveButtonClick',
			'onUserAndGroupGroupTabUsersShow'
		],

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.users.UsersView}
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

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.users.UsersView', { delegate: this });

			// Shorthands
			this.availableGrid = this.view.availableGrid;
			this.selectedGrid = this.view.selectedGrid;
		},

		/**
		 * Disable tab on add button click
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUsersAddButtonClick: function () {
			this.view.disable();
		},

		/**
		 * Enable/Disable tab evaluating selected group
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUsersGroupSelected: function () {
			this.view.setDisabled(this.cmfg('userAndGroupGroupSelectedGroupIsEmpty'));
		},

		/**
		 * @returns {Void}
		 *
		 * TODO: waiting for refactor (use an array of id not a string)
		 */
		onUserAndGroupGroupTabUsersSaveButtonClick: function () {
			var usersIdArray = [];

			Ext.Array.forEach(this.selectedGrid.getStore().getRange(), function (record, i, allRecords) {
				usersIdArray.push(record.get(CMDBuild.core.constants.Proxy.ID));
			}, this);

			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.USERS] = usersIdArray.join();

			CMDBuild.proxy.userAndGroup.group.Users.update({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					CMDBuild.core.Message.success();

					this.cmfg('onUserAndGroupGroupTabUsersShow');
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabUsersShow: function () {
			if (!this.cmfg('userAndGroupGroupSelectedGroupIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.ALREADY_ASSOCIATED] = false;

				this.availableGrid.getStore().load({ params: params });

				params[CMDBuild.core.constants.Proxy.ALREADY_ASSOCIATED] = true;

				this.selectedGrid.getStore().load({ params: params });
			}
		}
	});

})();
