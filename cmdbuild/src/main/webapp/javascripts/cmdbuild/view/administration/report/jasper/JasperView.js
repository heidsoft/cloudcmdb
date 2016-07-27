(function () {

	Ext.define('CMDBuild.view.administration.report.jasper.JasperView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.report.Jasper}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.report.jasper.form.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.report.jasper.GridPanel}
		 */
		grid: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
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
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addReport,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onReportJasperAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.report.jasper.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.report.jasper.form.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onReportJasperShow');
			}
		}
	});

})();
