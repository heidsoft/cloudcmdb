(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.privileges.tabs.DataView', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.privileges.DataView'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.tabs.DataView}
		 */
		delegate: undefined,

		border: false,
		disableSelection: true,
		frame: false,
		title: CMDBuild.Translation.views,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'none_privilege',
						text: CMDBuild.Translation.none,
						width: 60,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function (column, rowIndex, checked, eOpts) { // CheckColumn cannot be unchecked
								return checked;
							},
							checkchange: function (column, rowIndex, checked, eOpts) {
								this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabDataViewSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'none_privilege'
								});
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'read_privilege',
						text: CMDBuild.Translation.read,
						width: 60,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function (column, rowIndex, checked, eOpts) { // CheckColumn cannot be unchecked
								return checked;
							},
							checkchange: function (column, rowIndex, checked, eOpts) {
								this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabDataViewSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'read_privilege'
								});
							}
						}
					})
				],
				store: CMDBuild.proxy.userAndGroup.group.privileges.DataView.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabDataViewShow');
			}
		}
	});

})();
