(function () {

	Ext.define('CMDBuild.view.administration.workflow.tabs.taskManager.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.workflow.Tasks'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.tabs.TaskManager}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					Ext.create('Ext.ux.grid.column.Active', {
						dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
						text: CMDBuild.Translation.active,
						iconLabel: CMDBuild.Translation.active,
						align: 'center',
						width: 60,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					})
				],
				store: CMDBuild.proxy.workflow.Tasks.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onWorkflowTabTasksItemDoubleClick');
			},

			select: function (row, record, index) {
				this.delegate.cmfg('onWorkflowTabTasksRowSelect');
			}
		}
	});

})();
