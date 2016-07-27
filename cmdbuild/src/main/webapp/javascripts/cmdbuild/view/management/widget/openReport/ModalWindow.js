(function () {

	Ext.define('CMDBuild.view.management.widget.openReport.ModalWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.openReport.Modal}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.report,

		border: true,
		frame: false,
		layout: 'fit',
		overflowY: true,

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							'->',
							Ext.create('CMDBuild.core.buttons.iconized.Download', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWidgetOpenReportDownloadButtonClick');
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
				this.delegate.cmfg('onWidgetOpenReportModalWindowShow');
			}
		}
	});

})();
