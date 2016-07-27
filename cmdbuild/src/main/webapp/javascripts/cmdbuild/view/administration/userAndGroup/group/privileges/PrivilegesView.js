(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.privileges.PrivilegesView', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		title: CMDBuild.Translation.permissions,

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesShow');
			}
		}
	});

})();
