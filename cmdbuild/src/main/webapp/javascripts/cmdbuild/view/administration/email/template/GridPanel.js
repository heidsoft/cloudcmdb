(function() {

	Ext.define('CMDBuild.view.administration.email.template.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.email.Template'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.template.Template}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,
		split: true,

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
						flex: 3
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.SUBJECT,
						text: CMDBuild.Translation.subject,
						flex: 2
					}
				],
				store: CMDBuild.proxy.email.Template.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onEmailTemplateItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onEmailTemplateRowSelected');
			}
		}
	});

})();