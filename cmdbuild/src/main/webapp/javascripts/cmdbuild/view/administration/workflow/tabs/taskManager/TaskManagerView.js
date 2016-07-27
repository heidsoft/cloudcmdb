(function () {

	Ext.define('CMDBuild.view.administration.workflow.tabs.taskManager.TaskManagerView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.tabs.TaskManager}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.taskManager.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.taskManager,

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addTask,
								disablePanelFunctions: true,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabTasksAddButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyTask,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabTasksModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeTask,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabTasksRemoveButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.workflow.tabs.taskManager.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onWorkflowTabTasksShow');
			}
		}
	});

})();
