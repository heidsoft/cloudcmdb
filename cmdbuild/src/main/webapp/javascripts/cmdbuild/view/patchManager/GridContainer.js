(function() {

	Ext.define('CMDBuild.view.patchManager.GridContainer', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.patchManager.PatchManager}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.patchManager.GridPanel}
		 */
		grid: undefined,

		border: false,
		cls: 'cmdb-blue-panel',
		frame: false,
		layout: 'fit',
		padding: '0 0 5 0',

		initComponent: function() {
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
							Ext.create('CMDBuild.core.buttons.text.Apply', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onPatchManagerViewportApplyButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.patchManager.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();