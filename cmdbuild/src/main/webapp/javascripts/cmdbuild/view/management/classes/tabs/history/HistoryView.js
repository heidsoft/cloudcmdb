(function () {

	Ext.define('CMDBuild.view.management.classes.tabs.history.HistoryView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.classes.tabs.History}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.classes.tabs.history.GridPanel}
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
					this.grid = Ext.create('CMDBuild.view.management.classes.tabs.history.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onClassesTabHistoryPanelShow');
			}
		},

		/**
		 * Service function executed from module controller
		 *
		 * @returns {Void}
		 */
		reset: function () {
			this.setDisabled(this.delegate.cmfg('classesTabHistorySelectedEntityIsEmpty'));
		}
	});

})();
