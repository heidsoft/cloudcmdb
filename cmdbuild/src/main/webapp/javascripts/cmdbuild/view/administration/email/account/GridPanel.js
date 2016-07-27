(function () {

	Ext.define('CMDBuild.view.administration.email.account.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.email.Account'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Account}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,
		split: true,

		initComponent: function () {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.ux.grid.column.Tick', {
						text: CMDBuild.Translation.defaultLabel,
						dataIndex: CMDBuild.core.constants.Proxy.IS_DEFAULT,
						iconAltText: CMDBuild.Translation.defaultLabel,
						align: 'center',
						width: 50,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					}),
					{
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.ADDRESS,
						text: CMDBuild.Translation.address,
						flex: 1
					}
				],
				store: CMDBuild.proxy.email.Account.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onEmailAccountItemDoubleClick');
			},

			select: function (row, record, index) {
				this.delegate.cmfg('onEmailAccountRowSelected');
			}
		}
	});

})();
