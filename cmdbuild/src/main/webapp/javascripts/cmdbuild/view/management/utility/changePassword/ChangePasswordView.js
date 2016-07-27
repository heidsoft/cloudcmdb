(function () {

	Ext.define('CMDBuild.view.management.utility.changePassword.ChangePasswordView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.utility.changePassword.ChangePassword}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.changePassword.FormPanel}
		 */
		form: undefined,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: false,
		frame: false,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.management.utility.changePassword.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
