(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.users.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Users}
		 */
		delegate: undefined,

		border: true,
		frame: false,
		flex: 1,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						text: CMDBuild.Translation.username,
						dataIndex: CMDBuild.core.constants.Proxy.USERNAME,
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						flex: 1
					}
				],
				store: CMDBuild.proxy.userAndGroup.group.Users.getStoreGroupsUser()
			});

			this.callParent(arguments);
		}
	});

})();
