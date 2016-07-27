(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.privileges.UiConfiguration', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.privileges.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.tabs.Classes}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick',
			'onUserAndGroupGroupPrivilegesGridUIConfigurationSaveButtonClick',
			'onUserAndGroupGroupPrivilegesGridUIConfigurationShow',
			'userAndGroupGroupPrivilegesGridUIConfigurationRecordSet'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.model.userAndGroup.group.privileges.GridRecord}
		 */
		record: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.privileges.UiConfigurationWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.privileges.tabs.Classes} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.privileges.UiConfigurationWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick: function () {
			this.view.hide();
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupPrivilegesGridUIConfigurationSaveButtonClick: function () {
			var params = this.form.getForm().getValues();
			params[CMDBuild.core.constants.Proxy.CLASS_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);;
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			CMDBuild.proxy.userAndGroup.group.privileges.Classes.updateUIConfiguration({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					this.form.getForm().setValues(Ext.decode(decodedResponse)); // TODO: waiting for refactor

					this.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick');
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupPrivilegesGridUIConfigurationShow: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);;
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			CMDBuild.proxy.userAndGroup.group.privileges.Classes.readUIConfiguration({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					this.form.getForm().setValues(Ext.decode(decodedResponse));
				}
			});
		},

		/**
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} record
		 *
		 * @returns {Void}
		 */
		userAndGroupGroupPrivilegesGridUIConfigurationRecordSet: function (record) {
			if(!Ext.isEmpty(record))
				this.record = record;
		}
	});

})();
