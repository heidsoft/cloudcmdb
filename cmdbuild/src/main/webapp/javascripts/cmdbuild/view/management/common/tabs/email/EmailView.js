(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.EmailView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		delegate: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.email,

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						hidden: true,
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						}
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onTabEmailPanelShow');
			}
		},

		/**
		 * Service function executed from module controller
		 */
		reset: function () {
			this.setDisabled(this.delegate.cmfg('tabEmailSelectedEntityIsEmpty', CMDBuild.core.constants.Proxy.ENTITY));
		}
	});

})();
