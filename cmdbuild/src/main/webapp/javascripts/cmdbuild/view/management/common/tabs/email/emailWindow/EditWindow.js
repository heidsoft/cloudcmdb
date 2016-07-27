(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.emailWindow.EditWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.EmailWindow}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.MainContainer}
		 */
		attachmentContainer: undefined,

		/**
		 * @property {Ext.button.Split}
		 */
		fillFromTemplateButton: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.emailWindow.EditForm}
		 */
		form: undefined,

		title: CMDBuild.Translation.composeEmail,

		layout: 'border',

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [
							this.fillFromTemplateButton = Ext.create('Ext.button.Split', {
								iconCls: 'clone',
								text: CMDBuild.Translation.composeFromTemplate,

								handler: function () {
									this.showMenu();
								},

								menu: Ext.create('Ext.menu.Menu', {
									items: []
								})
							})
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
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTabEmailEmailWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTabEmailEmailWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('CMDBuild.view.management.common.tabs.email.emailWindow.EditForm', {
						delegate: this.delegate,
						region: 'center'
					}),
					this.attachmentContainer = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.MainContainer', {
						height: '20%',
						region: 'south'
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			beforedestroy: function (window, eOpts) {
				return this.delegate.cmfg('onTabEmailEmailWindowBeforeDestroy');
			}
		}
	});

})();
