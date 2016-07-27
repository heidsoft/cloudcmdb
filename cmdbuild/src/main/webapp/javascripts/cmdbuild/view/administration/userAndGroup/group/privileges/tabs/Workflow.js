(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.privileges.tabs.Workflow', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.privileges.Workflow'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.tabs.Workflow}
		 */
		delegate: undefined,

		border: false,
		disableSelection: true,
		frame: false,
		title: CMDBuild.Translation.processes,

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
								this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'none_privilege'
								});
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'read_privilege',
						text: CMDBuild.Translation.defaultLabel,
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
								this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'read_privilege'
								});
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: 'write_privilege',
						text: CMDBuild.Translation.defaultEnhanched,
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
								this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowSetPrivilege', {
									rowIndex: rowIndex,
									privilege: 'write_privilege'
								});
							}
						}
					}),
					Ext.create('Ext.grid.column.Action', {
						align: 'center',
						width: 50,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.filter.Set', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.rowAndColumnPrivileges,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowSetFilterClick', record);
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.filter.Clear', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.clearRowAndColumnPrivilege,
								scope: this,

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowRemoveFilterClick', record);
								}
							})
						]
					})
				],
				store: CMDBuild.proxy.userAndGroup.group.privileges.Workflow.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowShow');
			}
		}
	});

})();
