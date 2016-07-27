(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.filter.advanced.window.ColumnPrivilegesGridRecord'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.ColumnPrivileges}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.constants.Proxy.NONE,
						text: CMDBuild.Translation.none,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function(column, rowIndex, checked, eOpts) {
								if (checked)
									this.delegate.cmfg('onFieldFilterAdvancedWindowColumnPrivilegesSet', {
										dataIndex: column.dataIndex,
										rowIndex: rowIndex
									});

								return checked;
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.constants.Proxy.READ,
						text: CMDBuild.Translation.read,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function(column, rowIndex, checked, eOpts) {
								if (checked)
									this.delegate.cmfg('onFieldFilterAdvancedWindowColumnPrivilegesSet', {
										dataIndex: column.dataIndex,
										rowIndex: rowIndex
									});

								return checked;
							}
						}
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.constants.Proxy.WRITE,
						text: CMDBuild.Translation.write,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							beforecheckchange: function(column, rowIndex, checked, eOpts) {
								if (checked)
									this.delegate.cmfg('onFieldFilterAdvancedWindowColumnPrivilegesSet', {
										dataIndex: column.dataIndex,
										rowIndex: rowIndex
									});

								return checked;
							}
						}
					})
				],
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.common.field.filter.advanced.window.ColumnPrivilegesGridRecord',
					data: [],
					sorters: [
						{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' },
						{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
					]
				})
			});

			this.callParent(arguments);
		}
	});

})();