(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.privileges.tabs.CustomPage', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.userAndGroup.group.privileges.CustomPage'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.privileges.Privileges}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupTabPrivilegesTabCustomPageSetPrivilege',
			'onUserAndGroupGroupTabPrivilegesTabCustomPageShow'
		],

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.privileges.tabs.CustomPage}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.group.privileges.Privileges} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.privileges.tabs.CustomPage', { delegate: this });
		},

		/**
		 * @param {Object} parameters
		 * @param {Number} parameters.rowIndex
		 * @param {String} parameters.privilege
		 *
		 * @returns {Void}
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onUserAndGroupGroupTabPrivilegesTabCustomPageSetPrivilege: function (parameters) {
			if (!Ext.isEmpty(parameters) && Ext.isObject(parameters)) {
				var params = {};
				params['privilege_mode'] = parameters.privilege;
				params['privilegedObjectId'] = this.view.store.getAt(parameters.rowIndex).get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.userAndGroup.group.privileges.CustomPage.update({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.cmfg('onUserAndGroupGroupTabPrivilegesTabCustomPageShow');
					}
				});
			} else {
				_error('wrong or empty parameters in onUserAndGroupGroupTabPrivilegesTabCustomPageSetPrivilege()', this);
			}
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPrivilegesTabCustomPageShow: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			this.view.getStore().load({ params: params });
		}
	});

})();
