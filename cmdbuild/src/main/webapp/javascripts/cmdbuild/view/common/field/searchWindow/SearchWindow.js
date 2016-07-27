(function () {

	Ext.define('CMDBuild.view.common.field.searchWindow.SearchWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		/**
		 * @cfg {CMDBuild.controller.common.field.searchWindow.SearchWindow}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.AddCardMenuButton}
		 */
		addCardButton: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.list,

		/**
		 * @property {CMDBuild.core.buttons.text.Save}
		 */
		saveButton: undefined,

		closeAction: 'hide',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							this.addCardButton = Ext.create('CMDBuild.core.buttons.AddCardMenuButton')
						]
					}),
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
							this.saveButton = Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onFieldSearchWindowSaveButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (window, eOpts) {
				this.delegate.cmfg('onFieldSearchWindowShow');
			}
		}
	});

})();
