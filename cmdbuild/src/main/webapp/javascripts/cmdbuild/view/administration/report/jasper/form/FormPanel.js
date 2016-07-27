(function () {

	Ext.define('CMDBuild.view.administration.report.jasper.form.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.report.Jasper}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.report.jasper.form.Step1Panel}
		 */
		step1Panel: undefined,

		/**
		 * @property {CMDBuild.view.administration.report.jasper.form.Step2Panel}
		 */
		step2Panel: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'cmdb-border-top',
		frame: false,
		layout: 'card',

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
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyReport,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onReportJasperModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeReport,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onReportJasperRemoveButtonClick');
								}
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
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onReportJasperSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onReportJasperAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.step1Panel = Ext.create('CMDBuild.view.administration.report.jasper.form.Step1Panel'),
					this.step2Panel = Ext.create('CMDBuild.view.administration.report.jasper.form.Step2Panel')
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();
