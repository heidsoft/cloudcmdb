(function () {

	Ext.define('CMDBuild.view.management.workflow.tabs.history.HistoryView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.workflow.tabs.History}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.tabs.history.GridPanel}
		 */
		grid: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.history,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.management.workflow.tabs.history.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onWorkflowTabHistoryPanelShow');
			}
		},

		/**
		 * Service function executed from module controller
		 *
		 * @returns {Void}
		 */
		reset: function () {
			this.setDisabled(this.delegate.cmfg('workflowHistorySelectedEntityIsEmpty'));
		}
	});

})();
