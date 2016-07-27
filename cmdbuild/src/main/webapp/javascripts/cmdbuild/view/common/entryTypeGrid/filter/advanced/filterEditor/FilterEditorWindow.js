(function () {

	/**
	 * @link CMDBuild.view.common.field.filter.advanced.window.Window
	 */
	Ext.define('CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditorWindow', {
		extend: 'CMDBuild.core.window.AbstractCustomModal',

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.searchFilter,

		/**
		 * @cfg {String}
		 */
		dimensionsMode: 'percentage',

		/**
		 * @property {Ext.tab.Panel}
		 */
		wrapper: undefined,

		border: true,
		closeAction: 'hide',
		frame: true,
		layout: 'fit',

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
							Ext.create('CMDBuild.core.buttons.text.Apply', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorApplyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.SaveAndApply', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorSaveAndApplyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.wrapper = Ext.create('Ext.tab.Panel', {
						border: false,
						frame: false
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			hide: function (panel, eOpts) {
				this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorViewHide');
			},
			show: function (panel, eOpts) {
				this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorViewShow');
			}
		}
	});

})();
