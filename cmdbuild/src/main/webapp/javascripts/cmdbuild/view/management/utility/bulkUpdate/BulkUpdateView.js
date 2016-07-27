(function () {

	Ext.define('CMDBuild.view.management.utility.bulkUpdate.BulkUpdateView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.utility.bulkUpdate.BulkUpdate}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.bulkUpdate.ClassesTree}
		 */
		classesTree: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.bulkUpdate.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.bulkUpdate.GridPanel}
		 */
		grid: undefined,

		bodyCls: 'cmdb-blue-panel',
		border: false,
		frame: false,
		layout: 'border',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityBulkUpdateSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityBulkUpdateAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.classesTree = Ext.create('CMDBuild.view.management.utility.bulkUpdate.ClassesTree', {
						delegate: this.delegate,
						region: 'west',
						width: 200,
						split: true
					}),
					Ext.create('Ext.container.Container', {
						layout: 'border',
						region: 'center',

						items: [
							this.grid = Ext.create('CMDBuild.view.management.utility.bulkUpdate.GridPanel', {
								delegate: this.delegate,
								region: 'north',
								height: '70%',
								split: true
							}),
							this.form = Ext.create('CMDBuild.view.management.utility.bulkUpdate.FormPanel', {
								delegate: this.delegate,
								region: 'center'
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onUtilityBulkUpdatePanelShow');
			}
		}
	});

})();
